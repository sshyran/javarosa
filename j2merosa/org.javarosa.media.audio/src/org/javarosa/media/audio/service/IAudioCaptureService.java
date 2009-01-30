/**
 *   An interface to describe the supported functions of all types of AudioCapture services
 * 
 *   @author Ndubisi Onuora
 */

package org.javarosa.media.audio.service;

import org.javarosa.core.services.IService;
import org.javarosa.media.audio.AudioException;
import org.javarosa.utilities.file.FileException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public interface IAudioCaptureService extends IService 
{
	public static final int	IDLE = 0;
	public static final int CAPTURE_STARTED = 1; 
	public static final int CAPTURE_STOPPED = 2; 
	public static final int PLAYBACK_STARTED = 3;
	public static final int PLAYBACK_STOPPED = 4;
	public static final int CLOSED = 5;
	
	//Get the name of the service
	public String getName();
	
	//Returns the state of this service
	public int getState();
	
	//Start recording audio
	public void startRecord() throws AudioException;	
	
	//Stop recording audio
	public void stopRecord() throws AudioException;	
	
	//Start playing the recorded audio
	public void startPlayback() throws AudioException;
	
	//Stop playback of the recorded audio
	public void stopPlayback() throws AudioException;
	
	//Return the captured audio
	public OutputStream getAudio();
	
	//Retrieves the full path to the captured audio
	public String getAudioPath();
	
	//Saves the captured audio to a file
	public void saveRecording(String fileName) throws FileException;
	
	//Removes the captured audio
	public void removeRecording() throws FileException;
	
	//Closes all types of streams that are used
	public void closeStreams() throws IOException;
}