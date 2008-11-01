package org.javarosa.media.image.activity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IDisplay;
import org.javarosa.core.api.IShell;
import org.javarosa.j2me.view.DisplayViewFactory;
import org.javarosa.media.image.model.FileDataPointer;
import org.javarosa.media.image.storage.ImageRMSUtility;
import org.javarosa.media.image.utilities.FileUtility;
import org.javarosa.media.image.view.CameraCanvas;

/**
 * An Activity that represents the capture of a single Image.  This will talk to the
 * native device camera and return the selected image.
 * 
 * @author Cory Zue
 *
 */
public class ImageCaptureActivity implements IActivity, CommandListener
{

	private Context context;
	private IShell shell;
	

	// camera needed variables
	
	private Player mPlayer;
	private VideoControl mVideoControl;
	private Command mBackCommand;
	private Command mCaptureCommand;
	private IDisplay display;
	private ImageRMSUtility dataModel;

	public ImageCaptureActivity(IShell shell) {
		this.shell = shell;
		display = JavaRosaServiceProvider.instance().getDisplay();
		dataModel = new ImageRMSUtility("image_store");
	}

	public void contextChanged(Context globalContext) {
		// TODO Auto-generated method stub
		
	}

	public void destroy() {
		shell.returnFromActivity(this, "Success!", null);
	}

	public Context getActivityContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public void halt() {
		// TODO Auto-generated method stub
		
	}

	public void resume(Context globalContext) {
		// TODO Auto-generated method stub
		
	}

	public void setShell(IShell shell) {
		this.shell = shell;
	}

	public void start(Context context) {
		// initialize GUI
		// take a pointer to the context and shell
		this.context = context;
		showCamera();
		
	}
	
	/**
	 * Actually capture an image
	 * 
	 */
	private FileDataPointer captureImage() {
		return null;
	}

	
	/**
	 * takes the selected image return it (and control) to the shell
	 * Other images are deleted?
	 */
	private void finish() {
		Hashtable args = buildReturnArgs();
		shell.returnFromActivity(this, "Success!", args);

	}

	private Hashtable buildReturnArgs() {
		// stick the picture in here. 
		return null;
	}
	private void showCamera() {
		try {
			mPlayer = Manager.createPlayer("capture://video");
			mPlayer.realize();

			mVideoControl = (VideoControl) mPlayer.getControl("VideoControl");

//			Command mExitCommand = new Command("Exit", Command.EXIT, 0);
//			Command mCameraCommand = new Command("Camera", Command.SCREEN, 0);
			mBackCommand = new Command("Back", Command.BACK, 0);
			mCaptureCommand = new Command("Capture", Command.SCREEN, 0);

			Canvas canvas = new CameraCanvas(null, mVideoControl);
			canvas.addCommand(mBackCommand);
			canvas.addCommand(mCaptureCommand);
			canvas.setCommandListener(this);
			
			display.setView(DisplayViewFactory.createView(canvas));
			mPlayer.start();
		} catch (IOException ioe) {
			handleException(ioe);
		} catch (MediaException me) {
			handleException(me);
		}
	}

	private void handleException(Exception e) {
//		Alert a = new Alert(e.toString(), e.toString(), null, null);
//		a.setTimeout(Alert.FOREVER);
//		JavaRosaServiceProvider.instance().getDisplay().setCurrent(a);
//		throw new RuntimeException(e.getMessage());
		String toLog = e.getMessage();
		toLog += e.toString();
		saveFile("log" + System.currentTimeMillis() + ".txt", toLog.getBytes());
		
	}

	public void commandAction(Command cmd, Displayable display) {
		// TODO Auto-generated method stub
		if (cmd.equals(this.mBackCommand)) {
			goBack();
		}
		else if (cmd.equals(this.mCaptureCommand)) {
			doCapture();
			System.out.println("Click!");
		}
	}

	private void goBack() {
		// TODO
		System.out.println("Back Again!");
		this.shell.returnFromActivity(this, Constants.ACTIVITY_CANCEL, null);
		
	}

	private void doCapture() {
		byte[] jpg;
		int width = 1360;
		int height = 1020;
		try {
			// Get the image.
			jpg = mVideoControl.getSnapshot("encoding=jpeg&quality=100&width=" + width + "&height=" + height);
			// Save to file
			String fileName = "test" + System.currentTimeMillis();
			boolean saved = saveFile(fileName + ".jpg", jpg);
		} catch (MediaException me) {
			handleException(me);
		}
	}
	
	
	private void doCaptureLoop() {
		byte[] jpg;
		// add a loop to do this a lot and write them to individual files so we know when we fail
		int width = 640;
		int height = 480;
		int failures = 0;
		String text = "";
		while (failures < 3 && width < 3000) {
		try {
			text += width + "x" + height + ": ";
			// Get the image.
			//jpg = mVideoControl.getSnapshot("encoding=jpeg&quality="+ quality);
			jpg = mVideoControl.getSnapshot("encoding=jpeg&quality=100&width=" + width + "&height=" + height);
			String fileName = "test" + System.currentTimeMillis();
			boolean saved = saveFile(fileName + ".jpg", jpg);
			if (saved) {
				text += "Success!";
				
			}
			//jpg = mVideoControl.getSnapshot("encoding=jpeg&quality=100&width=2048&height=1536");
			//jpg = mVideoControl.getSnapshot("encoding=jpeg&quality=100&width=1280&height=960");
		} catch (MediaException me) {
			handleException(me);
			failures++;
			jpg = null;
			text += "Fail!";
		}
		text += "\n";
			width += 80;
			height += 60;
		}
		saveFile("photo_log" + System.currentTimeMillis() + ".txt", text.getBytes());
	}
	
	private boolean saveImageToRMS(String filename, byte[] image) {
		Image imageObj = Image.createImage(image, 0, image.length);
		dataModel.saveImage(filename, image);
		return true;
	}
	
	private boolean saveFile(String filename, byte[] image) {
		// TODO 
		String rootName = FileUtility.getDefaultRoot();
		String restorepath = "file:///" + rootName + "JRImages";				
		FileUtility.createDirectory(restorepath);
		String fullName = restorepath + "/" + filename;
		System.out.println("Image saved.");
		return FileUtility.createFile(fullName, image);
		// not sure why this was being done twice
	}

	
	
	public void showAlert(String error) {
		// TODO: should these be polished?
		Alert alert = new Alert(error);
		alert.setTimeout(Alert.FOREVER);
		alert.setType(AlertType.ERROR);
		//display.setView(alert);
	}
	
}