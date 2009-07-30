package org.javarosa.services.transport.impl.sms;

import java.util.Vector;

import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.BasicTransportMessage;

import de.enough.polish.io.Serializable;

/**
 * SMS transport - content is a Vector of strings
 * 
 */
public class SMSTransportMessage extends BasicTransportMessage implements
		Serializable {

	/**
	 * SMS messages can be no longer than 140 characters in length
	 */
	public final static int MAX_SIZE = 140;

	/**
	 * 
	 */
	private String destinationURL;

	/**
	 * @param str
	 * @param destinationURL
	 */
	public SMSTransportMessage(String str, String destinationURL) {
		this.destinationURL = destinationURL;
		setContent(splitSMS(str));
	}

	/**
	 * 
	 * SMS can be of maximum 140 characters in length.
	 * 
	 * If the message to be sent is greater, it is partitioned.
	 * 
	 * @param str
	 * @return Vector of strings to be sent as separate messages
	 */
	private Vector splitSMS(String str) {
		String message = str;
		Vector v = new Vector();

		// if message is too long split it
		while (message.length() > MAX_SIZE) {
			String part = message.substring(0, MAX_SIZE);
			v.addElement(part);
			message = message.substring(MAX_SIZE + 1);
		}

		// whatever remaining of the message after
		// chopping out 140 character length chunks
		// must also be added
		if (message.length() > 0)
			v.addElement(message);
		return v;
	}

	/**
	 * @return
	 */
	public String getDestinationURL() {
		return destinationURL;
	}

	/**
	 * @param destinationURL
	 */
	public void setDestinationURL(String destinationURL) {
		this.destinationURL = destinationURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.services.transport.TransportMessage#createTransporter()
	 */
	public Transporter createTransporter() {
		return new SMSTransporter(this);
	}

}