package edu.sunrover.source;

import java.io.InputStream;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;


public class SunRoverAudioOutput implements Runnable {
	
	byte[] buffer1 = new byte[16000];
	boolean running = true;
	boolean bufferToPlay = false;
	Socket client;
	
	public SunRoverAudioOutput(Socket socket) {
		try {
			client = socket;
			Thread t = new Thread(this);
			t.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void run() {
		try {
			InputStream in = client.getInputStream();
			AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
			AudioInputStream ais = new AudioInputStream(in, format, buffer1.length / format.getFrameSize());
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			SourceDataLine sline = (SourceDataLine)AudioSystem.getLine(info);
			sline.open(format);
			sline.start();
			int nBytesRead = 0;
			while (running == true) {
				while (nBytesRead != -1) {
					//System.out.println("Inside Loop " + nBytesRead);
					nBytesRead = ais.read(buffer1,0,buffer1.length);
					//System.out.println("After Read " + nBytesRead);
					if (nBytesRead >= 0) {
						sline.write(buffer1, 0, nBytesRead);
					}
				}
				nBytesRead = 0;
				ais = new AudioInputStream(in, format, buffer1.length / format.getFrameSize());
			}
			client.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}

}
