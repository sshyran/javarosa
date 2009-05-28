package org.javarosa.formmanager.view.transport;

import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.formmanager.activity.FormTransportActivity;

public class FormTransportViews {
	FormTransportActivity activity;
	// ----------- subviews
	private FormTransportMainMenu mainMenu;
	private FormTransportMessageList messageList;
	private TextBox loggingTextBox;
	private FormTransportMessageDetailBox messageDetailTextBox;
	private SendNowSendLaterForm sendNowSendLater;
	private FormTransportSubmitStatusScreen submitStatusScreen;
	private MultiSubmitStatusScreen multiSubmitStatusScreen;

	public FormTransportViews(FormTransportActivity activity) {
		super();
		this.activity = activity;
		this.mainMenu = constructMainMenu();
		this.messageList = new FormTransportMessageList(this.activity);
		this.loggingTextBox = createLoggingTextBox();
		this.messageDetailTextBox = new FormTransportMessageDetailBox(
				this.activity);
		this.sendNowSendLater = new SendNowSendLaterForm(this.activity);
		this.submitStatusScreen = new FormTransportSubmitStatusScreen(
				this.activity);

		this.multiSubmitStatusScreen = new MultiSubmitStatusScreen(this.activity);

	}

	private TextBox createLoggingTextBox() {
		TextBox box = new TextBox(JavaRosaServiceProvider.instance().localize("message.log"), null, 1000,
				TextField.UNEDITABLE);
		box.addCommand(FormTransportCommands.CMD_BACK);
		return box;

	}

	private FormTransportMainMenu constructMainMenu() {
		Vector mainMenuItems = FormTransportMainMenu.getMenuItems();
		String[] elements = new String[mainMenuItems.size()];
		mainMenuItems.copyInto(elements);
		return new FormTransportMainMenu(this.activity,
				JavaRosaServiceProvider.instance().localize("menu.transport"), Choice.IMPLICIT, elements, null);

	}

	public void destroyStatusScreen() {
		this.submitStatusScreen.destroy();
	}

	public FormTransportActivity getActivity() {
		return this.activity;
	}

	public List getMainMenu() {
		return this.mainMenu;
	}

	public FormTransportMessageList getMessageList() {
		return this.messageList;
	}

	public TextBox getLoggingTextBox() {
		return this.loggingTextBox;
	}

	public TextBox getMessageDetailTextBox() {
		return this.messageDetailTextBox;
	}

	public SendNowSendLaterForm getSendNowSendLaterScreen() {
		return this.sendNowSendLater;
	}

	public FormTransportSubmitStatusScreen getSubmitStatusScreen() {
		return this.submitStatusScreen;
	}

	public MultiSubmitStatusScreen getMultiSubmitStatusScreen() {
		return multiSubmitStatusScreen;
	}

}
