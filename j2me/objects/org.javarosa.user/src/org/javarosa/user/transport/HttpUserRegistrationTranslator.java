package org.javarosa.user.transport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.services.transport.impl.simplehttp.SimpleHttpTransportMessage;
import org.javarosa.user.api.RegisterUserState;
import org.javarosa.user.model.User;
import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class HttpUserRegistrationTranslator implements UserRegistrationTranslator<SimpleHttpTransportMessage>{
	
	User user;
	RegisterUserState s;
	IStorageUtility userStorage;

	public HttpUserRegistrationTranslator(User user, RegisterUserState s, IStorageUtility userStorage) {
		this.user = user;
		this.s = s;
		this.userStorage = userStorage;
	}
	
	public SimpleHttpTransportMessage getUserRegistrationMessage() {
		return s.buildHttpMesage(getStreamFromRegistration(createXmlRegistrationDoc(user)));
	}
	
	private InputStream getStreamFromRegistration(Document registration) {
		 XmlSerializer ser = new KXmlSerializer();
		 ByteArrayOutputStream bos = new ByteArrayOutputStream();
		 try {
			ser.setOutput(bos, null);
			registration.write(ser);
		} catch (IOException e) {
			// We don't actually want to ever fail on this report, 
			e.printStackTrace();
		}
		//Note: If this gets too big, we can just write a wrapper to stream bytes one at a time
		//to the array. It'll probably be the XML DOM itself which blows up the memory, though...
		 ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		 return bis;

	}

	public boolean readResponse(SimpleHttpTransportMessage message) {
		String body = message.getResponseBody();
		KXmlParser parser = new KXmlParser();
		try {
			parser.setInput(new ByteArrayInputStream(body.getBytes()),null);
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
			Document response = new Document();
			response.parse(parser);
			readResponseDocument(response);
			return true;
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			throw new RuntimeException("Error Parsing Server Response to User Registration!");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error Parsing Server Response to User Registration!");
		}
	}
	
	private void readResponseDocument(Document response) {
		boolean updates = false;
		for(int i = 0; i < response.getChildCount(); ++i) {
			Object o = response.getChild(i);
			if(!(o instanceof Element)) {
				continue;
			}
			Element e = (Element)o;
			if(e.getName().equals("response-message")) {
				//Do we want to actually just print out the message? That seems weird
				//given the internationalization
			} else if(e.getName().equals("user-data")){
				for(int j = 0; j < response.getChildCount(); ++j) {
					Object data = e.getChild(j);
					if(!(data instanceof Element)) {
						continue;
					}
					Element dataElement = (Element)data;
					String propertyName = dataElement.getAttributeValue(null, "key");
					String property = (String)dataElement.getChild(0);
					user.setProperty(propertyName, property);
					updates = true;
				}
			}
		}
		if(updates) {
			try {
				userStorage.write(user);
			} catch (StorageFullException e) {
				e.printStackTrace();
				throw new RuntimeException("User Storage is full! Trying to update user based on registration response from server!");
			}
		}
	}
	
	private Document createXmlRegistrationDoc(User u) {
		Document document = new Document();
		Element root = document.createElement(null,"registration");
		root.setNamespace("openrosa.org/user-registration");
		
		addChildWithText(root,"username",u.getUsername());
		
		addChildWithText(root,"password",u.getPassword());
		addChildWithText(root,"uuid",u.getUniqueId());
		
		addChildWithText(root,"date",DateUtils.formatDate(new Date(),DateUtils.FORMAT_ISO8601));
		
		addChildWithText(root, "registering_phone_id",PropertyManager._().getSingularProperty(JavaRosaPropertyRules.DEVICE_ID_PROPERTY));
		
		
		Element userData =  root.createElement(null,"user_data");
		
		for(Enumeration en = u.listProperties(); en.hasMoreElements() ;) {
			String property = (String)en.nextElement();
			Element data= userData.createElement(null,"data");
			data.setAttribute(null,"key",property);
			data.addChild(Element.TEXT, u.getProperty(property));
			userData.addChild(Element.ELEMENT, data);
		}
		root.addChild(Element.ELEMENT,userData);
		document.addChild(Element.ELEMENT, root);
		return document;
	}
	private void addChildWithText(Element parent, String name, String text) {
		Element e = parent.createElement(null,name);
		e.addChild(Element.TEXT, text);
		parent.addChild(Element.ELEMENT, e);
	}
}
