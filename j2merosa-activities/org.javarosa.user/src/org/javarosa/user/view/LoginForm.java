/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.user.view;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;

import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IView;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.user.model.User;
import org.javarosa.user.storage.UserRMSUtility;
import org.javarosa.user.utility.LoginContext;

import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.TextField;

public class LoginForm extends FramedForm implements IView {

	private final static int DEFAULT_COMMAND_PRIORITY = 1;

	// #if javarosa.login.demobutton
	private StringItem demoButton;
	public final static Command CMD_DEMO_BUTTON = new Command(Localization.get("menu.Demo"),
			Command.ITEM, DEFAULT_COMMAND_PRIORITY);
	// #endif

	public final static Command CMD_CANCEL_LOGIN = new Command(Localization.get("menu.Exit"),
			Command.SCREEN, DEFAULT_COMMAND_PRIORITY);
	public final static Command CMD_LOGIN_BUTTON = new Command(Localization.get("menu.Login"),
			Command.ITEM, DEFAULT_COMMAND_PRIORITY);

	private StringItem loginButton;
	private TextField usernameField;
	private TextField passwordField;
	private UserRMSUtility userRMS;
	private User loggedInUser;

	// context attributes
	private final static String CTX_USERNAME = "username";
	private final static String CTX_PASSWORD = "password";
	private final static String CTX_COMMCARE_VERSION = "COMMCARE_VERSION";
	private final static String CTX_COMMCARE_BUILD = "COMMCARE_BUILD";
	private final static String CTX_JAVAROSA_BUILD = "JAVAROSA_BUILD";

	/**
	 * @param loginActivity
	 * @param title
	 */
	private final static int DEFAULT_ADMIN_USERID = -1;

	public LoginForm() {
		super(Localization.get("form.login.login"));
		init();
	}

	/**
	 * @param loginActivity
	 * @param title
	 */
	public LoginForm(IActivity loginActivity, String title) {
		super(title);
		init();
	}

	/**
	 * @param loginActivity
	 */
	private void init() {
		this.userRMS = new UserRMSUtility(UserRMSUtility.getUtilityName());

		// #debug debug
		System.out.println("userRMS:" + this.userRMS);
		boolean recordsExist = this.userRMS.getNumberOfRecords() > 0;

		// #debug debug
		System.out.println("records in RMS:" + recordsExist);

		User tempUser = new User();
		tempUser.setUsername("");// ? needed

		if (recordsExist) {
			// get first username from RMS

			int userId = this.userRMS.getNextRecordID() - 1;
			try {
				this.userRMS.retrieveFromRMS(userId, tempUser);
			} catch (Exception e) {
				e.printStackTrace();
				// do nothing for the user
			}

		}
		initLoginControls(tempUser.getUsername());

		showVersions();

	}

	/**
	 * @param username
	 */
	private void initLoginControls(String username) {

		// create the username and password input fields
		this.usernameField = new TextField(Localization.get("form.login.username"), username, 50,
				TextField.ANY);
		this.passwordField = new TextField(Localization.get("form.login.password"), "", 10,
				TextField.PASSWORD);

		// TODO:what this?
		addCommand(CMD_CANCEL_LOGIN);

		append(this.usernameField);
		append(this.passwordField);

		// set the focus on the password field
		this.focus(this.passwordField);
		this.passwordField.setDefaultCommand(CMD_LOGIN_BUTTON);

		// add the login button
		this.loginButton = new StringItem(null, Localization.get("form.login.login"), Item.BUTTON);
		append(this.loginButton);
		this.loginButton.setDefaultCommand(CMD_LOGIN_BUTTON);

		// #if javarosa.login.demobutton
		this.demoButton = new StringItem(null, "DEMO", Item.BUTTON);
		append(this.demoButton);
		this.demoButton.setDefaultCommand(CMD_DEMO_BUTTON);
		// #endif

	}

	/**
	 * 
	 */
	private void showVersions() {
		//#if javarosa.login.showbuild
		//String ccv = (String) this.parent.getActivityContext().getElement(
		//		CTX_COMMCARE_VERSION);
		//String ccb = (String) this.parent.getActivityContext().getElement(
		//		CTX_COMMCARE_BUILD);
		//String jrb = (String) this.parent.getActivityContext().getElement(
		//		CTX_JAVAROSA_BUILD);

		//if (ccv != null && !ccv.equals("")) {
		//	this.append(Localization.get("form.login.commcareversion") + " " + ccv);
		//}
		//if ((ccb != null && !ccb.equals(""))
		//		&& (jrb != null && !jrb.equals(""))) {
		//	this.append(Localization.get("form.login.buildnumber") + " " + ccb + "-" + jrb);
		//}
		//#endif
	}

	/**
	 * 
	 * After login button is clicked, activity asks form to validate user
	 * 
	 * @return
	 */
	public boolean validateUser() {

		String usernameEntered = this.usernameField.getString().trim();
		String passwordEntered = this.passwordField.getString().trim();

		// /find user in RMS:
		User userInRMS = new User();

		int index = 1;

		while (index <= this.userRMS.getNumberOfRecords()) {
			try {
				this.userRMS.retrieveFromRMS(index, userInRMS);
			} catch (Exception ioe) {
				ioe.printStackTrace();
			}
			if (userInRMS.getUsername().equalsIgnoreCase(usernameEntered)) {
				if (userInRMS.getPassword().equals(passwordEntered)) {
					setLoggedInUser(userInRMS);
					return true;
				}
			}

			index++;
		}

		return false;

	}

	/**
	 * @param passwordMode
	 */
	public void setPasswordMode(String passwordMode) {
		if (LoginContext.PASSWORD_FORMAT_NUMERIC.equals(passwordMode)) {
			this.passwordField.setConstraints(TextField.PASSWORD
					| TextField.NUMERIC);
		} else if (LoginContext.PASSWORD_FORMAT_ALPHA_NUMERIC
				.equals(passwordMode)) {
			this.passwordField.setConstraints(TextField.PASSWORD);
		}
	}

	/**
	 * @return
	 */
	public Alert successfulLoginAlert() {
		// Clayton Sims - May 27, 2009 : I changed this back to not force it to be a J2ME Alert, 
		// so that polish could style it and it wouldn't look terrible. Is there a reason it
		// was hardcoded to do that? I've seen this happen a few times before, so someone's clearly 
		// doing this for a reason.
		
		//#style mailAlert
		return new Alert(Localization.get("form.login.login.successful"),
				Localization.get("form.login.loading.profile"), null, AlertType.CONFIRMATION);

	}

	public UserRMSUtility getUserRMS() {
		return this.userRMS;
	}

	public String getPassWord() {
		return this.passwordField.getString();

	}

	public String getUserName() {
		return this.usernameField.getString();

	}

	public User getLoggedInUser() {
		return this.loggedInUser;
	}

	public void setLoggedInUser(User loggedInUser) {
		this.loggedInUser = loggedInUser;
	}

	public Object getScreenObject() {
		return this;
	}

	public TextField getPasswordField() {
		return this.passwordField;
	}

	// ------------------------

	public StringItem getLoginButton() {
		return this.loginButton;
	}

}