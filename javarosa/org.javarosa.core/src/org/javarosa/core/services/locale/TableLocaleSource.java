/**
 * 
 */
package org.javarosa.core.services.locale;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.OrderedHashtable;
import org.javarosa.core.util.UnregisteredLocaleException;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapMap;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * @author Clayton Sims
 * @date May 26, 2009 
 *
 */
public class TableLocaleSource implements LocaleDataSource {
	private OrderedHashtable localeData; /*{ String -> String } */
	
	public TableLocaleSource() {
		localeData = new OrderedHashtable();
	}
	
	
	/**
	 * Set a text mapping for a single text handle for a given locale.
	 * 
	 * @param textID Text handle. Must not be null. Need not be previously defined for this locale.
	 * @param text Localized text for this text handle and locale. Will overwrite any previous mapping, if one existed.
	 * If null, will remove any previous mapping for this text handle, if one existed.
	 * @throws UnregisteredLocaleException If locale is not defined or null.
	 * @throws NullPointerException if textID is null
	 */
	public void setLocaleMapping (String textID, String text) {
		if(textID == null) {
			throw new NullPointerException("Null textID when attempting to register " + text + " in locale table");
		}
		if (text == null) {
			localeData.remove(textID);			
		} else {
			localeData.put(textID, text);
		}
	}
	
	/**
	 * Determine whether a locale has a mapping for a given text handle. Only tests the specified locale and form; does
	 * not fallback to any default locale or text form.
	 * 
	 * @param textID Text handle.
	 * @return True if a mapping exists for the text handle in the given locale.
	 * @throws UnregisteredLocaleException If locale is not defined.
	 */
	public boolean hasMapping (String textID) {
		return (textID == null ? false : localeData.get(textID) != null);
	}
	
	
	public boolean equals(Object o) {
		if(!(o instanceof TableLocaleSource)) {
			return false;
		}
		TableLocaleSource l = (TableLocaleSource)o;
		return ExtUtil.equals(localeData, l.localeData);
	}

	public OrderedHashtable getLocalizedText() {
		return localeData;
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		localeData = (OrderedHashtable)ExtUtil.read(in, new ExtWrapMap(String.class, String.class, true), pf);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapMap(localeData));
	}
}
