/**
 * 
 */
package org.javarosa.chsreferral.view;

import java.io.IOException;

import javax.microedition.lcdui.Form;

import org.javarosa.chsreferral.model.PatientReferral;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IView;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.patient.model.Patient;
import org.javarosa.patient.storage.PatientRMSUtility;

/**
 * @author Clayton Sims
 * @date Jan 23, 2009 
 *
 */
public class ReferralsDetailView extends Form implements IView {
	PatientReferral ref;

	public ReferralsDetailView(String title) {
		super(title);
	}
	
	public void setReferral(PatientReferral ref) {
		this.ref = ref;
		
		PatientRMSUtility prms = (PatientRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(PatientRMSUtility.getUtilityName());
		Patient p = new Patient();
		try {
			prms.retrieveFromRMS(ref.getPatientId(), p);
		} catch (IOException e) {
		} catch (DeserializationException e) {
		}
				
		// Putting it in swahili.
		//#if commcare.lang.sw
		this.append("Jina: " + p.getName()); // Name
		this.append("Tarehe ya rufaa: " + DateUtils.formatDate(ref.getDateReferred(), DateUtils.FORMAT_HUMAN_READABLE_SHORT)); // Date of referral
		this.append("Aina ya rufaa: " + ref.getType()); // Referral type
		//#else
		this.append("Name: " + p.getName()); // Name
		this.append("Date of referral: " + DateUtils.formatDate(ref.getDateReferred(), DateUtils.FORMAT_HUMAN_READABLE_SHORT)); // Date of referral
		this.append("Referral type: " + ref.getType()); // Referral type
		//#endif
	}
	
	public PatientReferral getReferral() {
		return ref;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IView#getScreenObject()
	 */
	public Object getScreenObject() {
		return this;
	}

}
