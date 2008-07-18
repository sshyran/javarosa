/*
 * FormList.java
 *
 * Created on 2007/10/25, 10:23:19
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

// merge this with Form list to 'metadata list'

package org.javarosa.formmanager.activity;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Ticker;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.core.model.FormData;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.storage.FormDataMetaData;
import org.javarosa.core.model.storage.FormDataRMSUtility;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.services.storage.utilities.UnavailableExternalizerException;
import org.javarosa.core.services.transport.TransportMessage;

/**
 *
 * @author Munier
 */
public class ModelListActivity extends List implements CommandListener, IActivity
{
	
	public final static String returnKey = "ModelListReturnCommand";

	public final static Command CMD_BACK = new Command("Back", Command.BACK, 2);
	public final static Command CMD_SEND = new Command("SEND Data",Command.SCREEN,1);
	public final static Command CMD_EDIT = new Command("Edit", Command.SCREEN, 2);
	public final static Command CMD_REFRESH = new Command("Refresh", Command.SCREEN, 3);
	public final static Command CMD_MSGS = new Command("Message Status",Command.SCREEN,4);
	public final static Command CMD_DELETE = new Command("Delete",Command.SCREEN,5);
	public final static Command CMD_EMPTY = new Command("Empty", Command.SCREEN, 6);
	
	Context theContext;

	public void contextChanged(Context globalContext) {
		theContext.mergeInContext(globalContext);
		//TODO: Do we have any values that need updating depending
		//on global context changes?
	}

	public void destroy() {
		// Stub. Nothing to do for this Module
	}

	public void halt() {
		// Stub. Nothing to do for this Module
	}

	public void resume(Context globalContext) {
		this.contextChanged(globalContext);
		this.createView();
	}

	public void start(Context context) {
		theContext = context;
		this.createView();
	}

    private FormDataRMSUtility formDataRMSUtility;
    private FormDefRMSUtility formDefRMSUtility;
    private IShell mainShell;
    private Vector modelIDs;

	private Image unSentImage;
	private Image deliveredImage;
	private Image unConfirmedImage;
	private Image failedImage;


    public ModelListActivity(IShell mainShell)
    {
        super("Saved Forms", List.EXCLUSIVE);
        this.mainShell = mainShell;
        this.formDataRMSUtility = (FormDataRMSUtility) JavaRosaServiceProvider
				.instance().getStorageManager().getRMSStorageProvider()
				.getUtility(FormDataRMSUtility.getUtilityName());
		this.formDefRMSUtility = (FormDefRMSUtility) JavaRosaServiceProvider
				.instance().getStorageManager().getRMSStorageProvider()
				.getUtility(FormDefRMSUtility.getUtilityName());
    }
    public void createView(){

    	unSentImage = initialiseStateImage(12, 12, 255, 255, 255);
    	deliveredImage = initialiseStateImage(12, 12, 0, 255, 0);
    	unConfirmedImage = initialiseStateImage(12, 12, 255, 140, 0);
    	failedImage = initialiseStateImage(12, 12, 255, 0, 0);
    	this.deleteAll();
    	this.setTicker(new Ticker("Please select a Model to send..."));
        this.addCommand(CMD_BACK);
        this.addCommand(CMD_EDIT);
        this.addCommand(CMD_SEND);
        this.addCommand(CMD_MSGS);
        this.addCommand(CMD_DELETE);
        this.addCommand(CMD_EMPTY);
        this.addCommand(CMD_REFRESH);
        this.setCommandListener(this);
        this.populateListWithModels();
        mainShell.setDisplay(this,this);
    }


    public void commandAction(Command c, Displayable d)
    {
        if (c == CMD_EDIT)
        {
            try
            {
            	if (this.getSelectedIndex() == -1) {
            		//error
            	} else {
            		FormDataMetaData data = (FormDataMetaData) modelIDs.elementAt(this.getSelectedIndex());
            		FormDef selectedForm = new FormDef();
        			//#if debug.output==verbose
            		System.out.println("Attempt retreive: "+data.getFormIdReference());
        			//#endif
            		this.formDefRMSUtility.retrieveFromRMS(data.getFormIdReference(), selectedForm);
        			//#if debug.output==verbose
            		System.out.println("Form retrieve OK\nAttempt retreive model: "+data.getRecordId());
            		//#endif
            		FormData formData = new FormData();
            		this.formDataRMSUtility.retrieveFromRMS(data.getRecordId(), formData);
            		selectedForm.setName(this.formDefRMSUtility.getName(data.getFormIdReference()));
            		Hashtable formEditArgs = new Hashtable();
            		formEditArgs.put(returnKey, CMD_EDIT);
            		formEditArgs.put("form", selectedForm);
            		formEditArgs.put("data", formData);
            		mainShell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, formEditArgs);
            	}
            } catch (Exception ex)//IOException ex)
            {
    			//#if debug.output==verbose || debug.output==exception
                ex.printStackTrace();
                //#endif
            }
        } else if (c == CMD_SEND)
        {
            if (this.getSelectedIndex() != -1) {
            	FormDataMetaData data = (FormDataMetaData) modelIDs.elementAt(this.getSelectedIndex());
            	FormData model = new FormData();
                try {
                    this.formDataRMSUtility.retrieveFromRMS(data.getRecordId(), model);
                    model.setRecordId(data.getRecordId());
                } catch (IOException e) {
                    javax.microedition.lcdui.Alert a = new javax.microedition.lcdui.Alert("modelLoadError", "Error Loading Model", null, AlertType.ERROR);
                    mainShell.setDisplay(this, a);
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    javax.microedition.lcdui.Alert a = new javax.microedition.lcdui.Alert("modelLoadError", "Error Loading Model", null, AlertType.ERROR);
                    mainShell.setDisplay(this, a);
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    javax.microedition.lcdui.Alert a = new javax.microedition.lcdui.Alert("modelLoadError", "Error Loading Model", null, AlertType.ERROR);
                    mainShell.setDisplay(this, a);
                    e.printStackTrace();
                } catch (UnavailableExternalizerException e) {
                    javax.microedition.lcdui.Alert a = new javax.microedition.lcdui.Alert("modelLoadError", "Error Loading Model", null, AlertType.ERROR);
                    mainShell.setDisplay(this, a);
                    e.printStackTrace();
                } 
                Hashtable formSendArgs = new Hashtable();
                //TODO: We need some way to codify this Next Action stuff. Maybe a set of Constants for the ModelListModule?
                formSendArgs.put(returnKey, CMD_SEND);
                formSendArgs.put("data", model);
                mainShell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, formSendArgs);
            }
        } else if (c == CMD_EMPTY)
        {
        	this.formDataRMSUtility.tempEmpty();
            createView();
        } else if (c == CMD_BACK)
        {
        	mainShell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, null);
        } else if (c == CMD_DELETE)
        {
        	FormDataMetaData data = (FormDataMetaData) modelIDs.elementAt(this.getSelectedIndex());
            formDataRMSUtility.deleteRecord(data.getRecordId());
            this.createView();
        } else if (c == CMD_MSGS)
        {	
        	//TODO: This is a phenomenal chance to try out the "inherited menus". Should look into that. 
        	Hashtable returnArgs = new Hashtable();
        	returnArgs.put(returnKey, CMD_MSGS);
        	mainShell.returnFromActivity(this, Constants.ACTIVITY_NEEDS_RESOLUTION, returnArgs);

        } else if (c == CMD_REFRESH)
        {
        	this.createView();
        }
    }

    public void populateListWithModels()
    {

    	this.formDataRMSUtility.open();
    	RecordEnumeration recordEnum = this.formDataRMSUtility.enumerateMetaData();
    	modelIDs = new Vector();
    	int pos =0;
    	while(recordEnum.hasNextElement())
    	{
    		int i;
			try {

				i = recordEnum.nextRecordId();
				FormDataMetaData mdata = new FormDataMetaData();
				this.formDataRMSUtility.retrieveMetaDataFromRMS(i,mdata);
				// TODO fix it so that record id is part of the metadata serialization
				mdata.setRecordId(i);

				Image stateImg = getStateImage(getModelDeliveryStatus(i));

				this.append(mdata.getRecordId()+"-"+mdata.getName()+"_"+mdata.getDateSaved()+"_"+mdata.getRecordId(), stateImg);
				modelIDs.insertElementAt(mdata,pos);
				pos++;
			} catch (InvalidRecordIDException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }

    private Image getStateImage(int modelDeliveryStatus) {
    	Image result;
    	switch (modelDeliveryStatus) {
		case TransportMessage.STATUS_DELIVERED:
			result = this.deliveredImage;
			break;
		case TransportMessage.STATUS_NEW:
			result = this.unConfirmedImage;
			break;
		case TransportMessage.STATUS_NOT_SENT:
			result = this.unSentImage;
			break;
		case TransportMessage.STATUS_FAILED:
			result = this.failedImage;
			break;
		default:
			result = this.unSentImage;
			break;
		}
		return result;
	}

	private Image initialiseStateImage(int w, int h, int r, int g, int b) {
		Image res = Image.createImage(w, h);
		Graphics gc = res.getGraphics();
		gc.setColor(r, g, b);
		gc.fillRect(0, 0, w, h);
		return res;
	}

	private int getModelDeliveryStatus(int modelId) {

		//TODO: Are we OK with using the transport manager here? There's coupling...
		Enumeration qMessages = JavaRosaServiceProvider.instance().getTransportManager().getMessages();
		//TODO: The way we're doing this is fairly wasteful. We should store them
		//locally, and update on change, instead of getting each one.
		TransportMessage message;
		while(qMessages.hasMoreElements())
    	{
			message = (TransportMessage) qMessages.nextElement();
			if(message.getModelId()==modelId)
				return message.getStatus();

    	}
		return 0;
	}
}