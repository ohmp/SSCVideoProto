package org.witness.ssc.video;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Vector;

import android.content.Context;
import android.util.Log;

public class FFMPEGWrapper {

	String[] libraryAssets = {"ffmpeg"};
	File fileBinDir;
	Context context;

	public FFMPEGWrapper(Context _context) throws FileNotFoundException, IOException {
		context = _context;
		fileBinDir = context.getDir("bin",0);

		if (!new File(fileBinDir,libraryAssets[0]).exists())
		{
			BinaryInstaller bi = new BinaryInstaller(context,fileBinDir);
			bi.installFromRaw();
		}
	}
	
	
	
	private void execProcess(String[] commands) throws IOException {		
        
	    	Process process = new ProcessBuilder(commands).redirectErrorStream(true).start();         	
			

			OutputStream outputStream = process.getOutputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	
			String line;
			
			//Log.d(ObscuraApp.LOGTAG,"***Starting Command***");
			while ((line = reader.readLine()) != null)
			{
				//Log.d(ObscuraApp.LOGTAG,"***"+line+"***");
			}
			//Log.d(ObscuraApp.LOGTAG,"***Ending Command***");

			
			
		    if (process != null) {
		    	process.destroy();        
		    }

	}
	
	public void processVideo(File redactSettingsFile, 
			Vector<ObscureRegion> obscureRegions, File inputFile, File outputFile, 
			int width, int height, int frameRate, int kbitRate, float sizeMult) throws IOException {
		
		writeRedactData(redactSettingsFile, obscureRegions, sizeMult);
		    	
    	String cmd = new File(fileBinDir,"ffmpeg").getAbsolutePath();
    	//ffmpeg -v 10 -y -i /sdcard/org.witness.sscvideoproto/videocapture1042744151.mp4 -vcodec libx264 -b 3000k -s 720x480 -r 30 -acodec copy -f mp4 -vf 'redact=/data/data/org.witness.sscvideoproto/redact_unsort.txt' /sdcard/org.witness.sscvideoproto/new.mp4
    	String[] ffmpegCommand = {cmd, "-v", "10", "-y", "-i", inputFile.getPath(), 
				"-vcodec", "libx264", "-b", kbitRate+"k", "-s",  (int)(width*sizeMult) + "x" + (int)(height*sizeMult), "-r", ""+frameRate,
				"-an",
				"-f", "mp4",
				"-vf","redact=" + redactSettingsFile.getAbsolutePath(),
				outputFile.getPath()};
    	
    	
    	//"-vf" , "redact=" + Environment.getExternalStorageDirectory().getPath() + "/" + PACKAGENAME + "/redact_unsort.txt",

    	
    	// Need to make sure this will create a legitimate mp4 file
    	//"-acodec", "ac3", "-ac", "1", "-ar", "16000", "-ab", "32k",
    	//"-acodec", "copy",

    	/*
    	String[] ffmpegCommand = {"/data/data/"+PACKAGENAME+"/ffmpeg", "-v", "10", "-y", "-i", recordingFile.getPath(), 
    					"-vcodec", "libx264", "-b", "3000k", "-vpre", "baseline", "-s", "720x480", "-r", "30",
    					//"-vf", "drawbox=10:20:200:60:red@0.5",
    					"-vf" , "\"movie="+ overlayImage.getPath() +" [logo];[in][logo] overlay=0:0 [out]\"",
    					"-acodec", "copy",
    					"-f", "mp4", savePath.getPath()+"/output.mp4"};
    	*/
    	
    	execProcess(ffmpegCommand);
	    
	}
	
	private void writeRedactData(File redactSettingsFile, Vector<ObscureRegion> obscureRegions, float sizeMult) throws IOException {
		// Write out the finger data
					
		FileWriter redactSettingsFileWriter = new FileWriter(redactSettingsFile);
		PrintWriter redactSettingsPrintWriter = new PrintWriter(redactSettingsFileWriter);
		
		for (int i = 0; i < obscureRegions.size(); i++) {
			ObscureRegion or = (ObscureRegion)obscureRegions.get(i);
		//	Log.v(ObscuraApp.LOGTAG,"Writing: " + or.getStringData(sizeMult));
			redactSettingsPrintWriter.println(or.getStringData(sizeMult));
		}
		redactSettingsPrintWriter.flush();
		redactSettingsPrintWriter.close();

				
	}
	
	class FileMover {

		InputStream inputStream;
		File destination;
		
		public FileMover(InputStream _inputStream, File _destination) {
			inputStream = _inputStream;
			destination = _destination;
		}
		
		public void moveIt() throws IOException {
		
			OutputStream destinationOut = new BufferedOutputStream(new FileOutputStream(destination));
				
			int numRead;
			byte[] buf = new byte[1024];
			while ((numRead = inputStream.read(buf) ) >= 0) {
				destinationOut.write(buf, 0, numRead);
			}
			    
			destinationOut.flush();
			destinationOut.close();
		}
	}

}

