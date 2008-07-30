package org.javarosa.patient.activity;

import org.javarosa.core.api.IShell;
import org.javarosa.entitymgr.activity.EntitySelectActivity;
import org.javarosa.patientmgr.Constants;
import org.javarosa.patientmgr.model.Patient;


/**
 * An activity to search for a list of patients and then select one.
 * 
 * @author daniel
 *
 */
public class PatientSelectActivity extends EntitySelectActivity{
	
	/** Key for the selected patient. */
	public static final String KEY_PATIENT = KEY_ENTITY;
	
	/**
	 * Constructs a new patient select activity.
	 * 
	 * @param appTitle the title of the midlet.
	 */
	public PatientSelectActivity(IShell shell,String appTitle){
		super(new Patient().getClass(),appTitle,Constants.LABEL_PATIENT_ENTITY,Constants.LABEL_PATIENT_IDENTIFIER, Constants.LABEL_PATIENT_NAME,shell);
		//displayDetailsScreen(false);
	}
}