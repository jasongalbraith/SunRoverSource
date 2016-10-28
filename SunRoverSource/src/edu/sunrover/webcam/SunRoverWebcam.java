package edu.sunrover.webcam;

import java.awt.image.BufferedImage;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.*;
import com.googlecode.javacv.OpenCVFrameGrabber;
import static com.googlecode.javacv.cpp.opencv_core.cvFlip;
import edu.sunrover.source.RoverImage;

public class SunRoverWebcam implements Runnable {

	final int FIRST_WEBCAM = 0;
	final int SECOND_WEBCAM = 1;
	int threadToStart = FIRST_WEBCAM;
	ServerSocket server;
	Socket client;
	ObjectOutputStream stream1;
	ObjectOutputStream stream2;
	FrameGrabber grabber1 = new OpenCVFrameGrabber(0);
	FrameGrabber grabber2 = new OpenCVFrameGrabber(1);
	BufferedImage bimg1;
	BufferedImage bimg2;
	RoverImage rimg1;
	RoverImage rimg2;

	public SunRoverWebcam() {
		try {
			server = new ServerSocket(1234);
			System.out.println("Waiting...");
			client = server.accept();
			System.out.println("Got Socket 1");
			stream1 = new ObjectOutputStream(client.getOutputStream());
			client = server.accept();
			System.out.println("Got Socket 2");
			stream2 = new ObjectOutputStream(client.getOutputStream());
			Thread t = new Thread(this);
			t.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new SunRoverWebcam();
	}

	public void run() {
		if (threadToStart == FIRST_WEBCAM) {
			try {
				// Start grabber to capture video
				grabber1.start();
				IplImage img1;
				while (true) {
					img1 = grabber1.grab();
					if (img1 != null) {
						// Flip image horizontally
						cvFlip(img1, img1, 1);
						// Show video frame in canvas
						bimg1 = img1.getBufferedImage();
						rimg1 = new RoverImage(bimg1);
						stream1.writeObject(rimg1);
						stream1.flush();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (threadToStart == SECOND_WEBCAM) {
			try {
				// Start grabber to capture video
				grabber2.start();
				IplImage img2;
				while (true) {
					img2 = grabber2.grab();
					if (img2 != null) {
						// Flip image horizontally
						cvFlip(img2, img2, 1);
						// Show video frame in canvas
						bimg2 = img2.getBufferedImage();
						rimg2 = new RoverImage(bimg2);
						stream2.writeObject(rimg2);
						stream2.flush();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}