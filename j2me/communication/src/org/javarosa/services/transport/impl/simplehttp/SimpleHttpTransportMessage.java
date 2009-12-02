package org.javarosa.services.transport.impl.simplehttp;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.BasicTransportMessage;
import org.javarosa.services.transport.impl.StreamsUtil;

/**
 * A message which implements the simplest Http transfer - plain text via POST
 * request
 * 
 */
public class SimpleHttpTransportMessage extends BasicTransportMessage {

	
	private byte[] content;
	/**
	 * An http url, to which the message will be POSTed
	 */
	private String url;

	/**
	 * Http response code
	 */
	private int responseCode;

	/**
	 * 
	 */
	private String responseBody;


	public SimpleHttpTransportMessage() {
		//ONLY FOR SERIALIZATION
	}
	
	/**
	 * @param str
	 * @param destinationURL
	 */
	public SimpleHttpTransportMessage(String str, String url) {
		content = str.getBytes();
		this.url = url;
	}

	/**
	 * @param str
	 * @param destinationURL
	 */
	public SimpleHttpTransportMessage(byte[] str, String url) {
		content = str;
		this.url = url;
	}

	/**
	 * @param is
	 * @param destinationURL
	 * @throws IOException
	 */
	public SimpleHttpTransportMessage(InputStream is, String url)
			throws IOException {

		content = StreamsUtil.readFromStream(is, -1);
		this.url = url;
	}

	public HttpRequestProperties getRequestProperties() {
		return new HttpRequestProperties();
	}

	public boolean isCacheable() {
		return true;
	}
	

	public Object getContent() {
		return content;
	}
	
	/**
	 * @return
	 */
	public int getResponseCode() {
		return responseCode;
	}

	/**
	 * @param responseCode
	 */
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * @return
	 */
	public String getResponseBody() {
		return responseBody;
	}

	/**
	 * @param responseBody
	 */
	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.services.transport.TransportMessage#getTransporter()
	 */
	public Transporter createTransporter() {
		return new SimpleHttpTransporter(this);
	}

	public String toString() {
		String s = "#" + getCacheIdentifier() + " (http)";
		if (getResponseCode() > 0)
			s += " " + getResponseCode();
		return s;
	}

	public InputStream getContentStream() {
		return new ByteArrayInputStream((byte[]) getContent());
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		super.readExternal(in, pf);
		url = ExtUtil.readString(in);
		responseCode = (int)ExtUtil.readNumeric(in);
		responseBody = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
		content = ExtUtil.readBytes(in);
	}
		

	public void writeExternal(DataOutputStream out) throws IOException {
		super.writeExternal(out);
		ExtUtil.writeString(out,url);
		ExtUtil.writeNumeric(out,responseCode);
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(responseBody));
		ExtUtil.writeBytes(out,content);
	}

}