package org.javarosa.storage.rms;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

import org.javarosa.core.services.storage.utilities.IRecordStorage;
import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
import org.javarosa.core.services.storage.utilities.RecordStorageException;

public class RMSRecordStorage implements IRecordStorage {
	
	private RecordStore store;
	
	private String name;
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.IRecordStorage#openAsRecordStorage(java.lang.String, boolean)
	 */
	public void openAsRecordStorage(String name, boolean createIfNotExist)
			throws RecordStorageException {
		try {
			this.name = name;
			store = RecordStore.openRecordStore(name, createIfNotExist);
		} catch (RecordStoreFullException e) {
			throw new RecordStorageException(e.getMessage());
		} catch (RecordStoreNotFoundException e) {
			throw new RecordStorageException(e.getMessage());
		} catch (RecordStoreException e) {
			throw new RecordStorageException(e.getMessage());
		}
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.IRecordStorage#addRecord(byte[], int, int)
	 */
	public void addRecord(byte[] data, int i, int length)
			throws RecordStorageException {
		try {
			store.addRecord(data, i, length);
		} catch (RecordStoreNotOpenException e) {
			throw new RecordStorageException(e.getMessage());
		} catch (RecordStoreFullException e) {
			throw new RecordStorageException(e.getMessage());
		} catch (RecordStoreException e) {
			throw new RecordStorageException(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.IRecordStorage#closeRecordStore()
	 */
	public void closeRecordStore() throws RecordStorageException {
		try {
			store.closeRecordStore();
		} catch (RecordStoreNotOpenException e) {
			throw new RecordStorageException(e.getMessage());
		} catch (RecordStoreException e) {
			throw new RecordStorageException(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.IRecordStorage#deleteRecord(int)
	 */
	public void deleteRecord(int recordId) throws RecordStorageException {
		try {
			store.deleteRecord(recordId);
		} catch (RecordStoreNotOpenException e) {
			throw new RecordStorageException(e.getMessage());
		} catch (InvalidRecordIDException e) {
			throw new RecordStorageException(e.getMessage());
		} catch (RecordStoreException e) {
			throw new RecordStorageException(e.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.IRecordStorage#deleteRecordStore()
	 */
	public void deleteRecordStore() throws RecordStorageException {
		try {
			//Open and close this store separately from this interface.
			RecordStore scoresRecordStore1 = RecordStore.openRecordStore(name,true);
			scoresRecordStore1.closeRecordStore();
			
			RecordStore.deleteRecordStore(name);
			
		} catch (RecordStoreNotFoundException e) {
			throw new RecordStorageException(e.getMessage());
		} catch (RecordStoreException e) {
			throw new RecordStorageException(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.IRecordStorage#enumerateRecords(java.lang.Object, java.lang.Object, boolean)
	 */
	public IRecordStoreEnumeration enumerateRecords() throws RecordStorageException {
		try {
			return new RMSEnumeration(store.enumerateRecords(null,null,false));
		} catch (RecordStoreNotOpenException e) {
			throw new RecordStorageException(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.IRecordStorage#getNextRecordID()
	 */
	public int getNextRecordID() throws RecordStorageException {
		try {
			return store.getNextRecordID();
		} catch (RecordStoreNotOpenException e) {
			throw new RecordStorageException(e.getMessage());
		} catch (RecordStoreException e) {
			throw new RecordStorageException(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.IRecordStorage#getNumRecords()
	 */
	public int getNumRecords() throws RecordStorageException {
		try {
			return store.getNumRecords();
		} catch (RecordStoreNotOpenException e) {
			throw new RecordStorageException(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.IRecordStorage#getRecord(int)
	 */
	public byte[] getRecord(int recordId) throws RecordStorageException {
		try {
			return store.getRecord(recordId);
		} catch (RecordStoreNotOpenException e) {
			throw new RecordStorageException(e.getMessage());
		} catch (InvalidRecordIDException e) {
			throw new RecordStorageException(e.getMessage());
		} catch (RecordStoreException e) {
			throw new RecordStorageException(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.IRecordStorage#getSize()
	 */
	public float getSize() throws RecordStorageException {
		try {
			return store.getSize();
		} catch (RecordStoreNotOpenException e) {
			throw new RecordStorageException(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.IRecordStorage#getSizeAvailable()
	 */
	public float getSizeAvailable() throws RecordStorageException {
		try {
			return store.getSizeAvailable();
		} catch (RecordStoreNotOpenException e) {
			throw new RecordStorageException(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.IRecordStorage#setRecord(int, byte[], int, int)
	 */
	public void setRecord(int recordId, byte[] data, int i, int length)
			throws RecordStorageException {
		try {
			store.setRecord(recordId, data, i, length);
		} catch (RecordStoreNotOpenException e) {
			throw new RecordStorageException(e.getMessage());
		} catch (InvalidRecordIDException e) {
			throw new RecordStorageException(e.getMessage());
		} catch (RecordStoreFullException e) {
			throw new RecordStorageException(e.getMessage());
		} catch (RecordStoreException e) {
			throw new RecordStorageException(e.getMessage());
		}
	}

}
