package org.javarosa.services.transport;

import java.util.Vector;

import org.javarosa.services.transport.impl.TransportException;

public interface TransportCache {

	String cache(TransportMessage message) throws TransportException;

	void decache(TransportMessage message) throws TransportException;

	void updateMessage(TransportMessage message) throws TransportException;

	TransportMessage findMessage(String id);

	int getCachedMessagesCount();

	Vector getCachedMessages();

	void clearCache();

}