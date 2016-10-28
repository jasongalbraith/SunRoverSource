package edu.sunrover.source;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;                                                                                                                                             
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;


public class SunRoverAudioInput implements Runnable, KeyListener {

	Socket client;
	int threadToRun = 0;
	int bufferSize;
	byte[] buffer1;
	byte[] buffer2;
	int count1;
	int count2;
	boolean running;
	boolean recording;
	TargetDataLine line;
	final AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
	//final AudioFormat format = new AudioFormat(8000.0f, 8, 1, true, true);
	OutputStream outputStream;
	boolean bufferFull = false;
	boolean keyPressed = false;

	public SunRoverAudioInput(Socket socket) {
		try {
			client = socket;
			bufferSize = (int) format.getSampleRate() * format.getFrameSize();
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format, bufferSize);
			line.start();
			buffer1 = new byte[bufferSize];
			buffer2 = new byte[bufferSize];
			outputStream = new BufferedOutputStream(socket.getOutputStream());
			Thread t = new Thread(this);
			threadToRun = 0;
			t.start();
			running = true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void run() {
		try {
			if (threadToRun == 0) {
				threadToRun = 1;
				Thread t = new Thread(this);
				t.start();
				while (running) {
					if (bufferFull == false) {
						count1 = line.read(buffer1, 0, buffer1.length);
						buffer2 = buffer1;
						count2 = count1;
						buffer1 = new byte[buffer2.length];
						count1 = 0;
						bufferFull = true;
					}
				}
			}
			else if (threadToRun == 1){
				while (running) {
					if (bufferFull == true) {
						if (recording == true) {
							System.out.println("Writing out...");
							outputStream.write(buffer2, 0, count2);
						}
						bufferFull = false;
					}
				}
			}
			outputStream.write(buffer1, 0, count2);
			buffer1 = new byte[bufferSize];
			buffer2 = new byte[bufferSize];
		} catch (IOException e) {
			System.exit(-1);
			System.out.println("exit");
		} catch (Exception e) {
			System.out.println("Direct Upload Error");
			e.printStackTrace();
		}
	}

	@Override
	public void keyPressed(KeyEvent key) {
		if (key.getKeyChar() == ' ' && keyPressed == false) {
			System.out.println("Recording!");
			keyPressed = true;
			recording = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent key) {
		if (key.getKeyChar() == ' ' && keyPressed == true) {
			System.out.println("Stopped recording!");
			keyPressed = false;
			recording = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent key) {
	}

}
