/**
 * 
 */
package org.javarosa.core.model.instance.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.storage.DataModelTreeRMSUtility;
import org.javarosa.core.model.utils.IDataModelSerializingVisitor;
import org.javarosa.core.services.transport.ByteArrayPayload;
import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.core.services.transport.IDataPayloadVisitor;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * The ModelReferencePayload essentially provides a wrapper functionality
 * around a ModelTree to allow it to be used as a payload, but only to
 * actually perform the various computationally expensive functions
 * of serialization when required.
 * 
 * @author Clayton Sims
 * @date Apr 27, 2009 
 *
 */
public class ModelReferencePayload implements IDataPayload {
	
	int recordId;
	IDataPayload payload;
	
	IDataModelSerializingVisitor serializer;
	
	//NOTE: Should only be used for serializaiton.
	public ModelReferencePayload() {
		
	}
	
	public ModelReferencePayload(int modelRecordId) {
		this.recordId = modelRecordId;
	}

	/**
	 * @param serializer the serializer to set
	 */
	public void setSerializer(IDataModelSerializingVisitor serializer) {
		this.serializer = serializer;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#accept(org.javarosa.core.services.transport.IDataPayloadVisitor)
	 */
	public Object accept(IDataPayloadVisitor visitor) {
		memoize();
		return payload.accept(visitor);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#getLength()
	 */
	public long getLength() {
		memoize();
		return payload.getLength();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#getPayloadId()
	 */
	public String getPayloadId() {
		memoize();
		return payload.getPayloadId();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#getPayloadStream()
	 */
	public InputStream getPayloadStream() {
		memoize();
		return payload.getPayloadStream();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#getPayloadType()
	 */
	public int getPayloadType() {
		memoize();
		return payload.getPayloadType();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		recordId = in.readInt();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeInt(recordId);
	}
	
	private void memoize() {
		if(payload == null) {
			DataModelTreeRMSUtility dataModelRMSUtility = (DataModelTreeRMSUtility) JavaRosaServiceProvider
			.instance().getStorageManager().getRMSStorageProvider()
			.getUtility(DataModelTreeRMSUtility.getUtilityName());
			
			DataModelTree tree = new DataModelTree();
			
			try {
			
				dataModelRMSUtility.retrieveFromRMS(recordId, tree);
				
				payload = serializer.createSerializedPayload(tree);
				
			} catch (IOException e) {
				//Assertion, do not catch!
				e.printStackTrace();
				throw new RuntimeException("ModelReferencePayload failed to retrieve its model from rms");
			} catch (DeserializationException e) {
				//Assertion, do not catch!
				e.printStackTrace();
				throw new RuntimeException("ModelReferencePayload failed to retrieve its model from rms");
			}
		}
	}
	
	public int getTransportId() {
		return recordId;
	}
}