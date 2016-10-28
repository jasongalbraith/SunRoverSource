package edu.sunrover.source;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;


@SuppressWarnings("serial")
public class ImagePanel extends JPanel implements Runnable {
	
	Image[] img = new Image[2];
	ImageIcon[] ii = new ImageIcon[2];
	ObjectInputStream[] streams = new ObjectInputStream[2];
	DataOutputStream dos;
	int width1;
	int height1;
	int width2;
	int height2;
	JFrame frame;
	final int FIRST_WEBCAM = 0;
	final int SECOND_WEBCAM = 1;
	final int OUTPUT = 2;
	int threadToRun = FIRST_WEBCAM;
	boolean running = true;
	JSlider scaleBar;
	double scaleValue = 0.25;
	
	public ImagePanel(JFrame frameIn, JSlider js) {
		frame = frameIn;
		scaleBar = js;
	}
	
	public JSlider getSlider() {return scaleBar;}
	
	public void setInputStream(ObjectInputStream in, int index) {
		streams[index] = in;
	}
	
	public void setOutputStream(DataOutputStream out) {
		dos = out;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (img[0] == null) {
			g.drawLine(0, 0, 400, 400);
		}
		else {
			g.drawImage(img[0], 0, 0, width1, height1, 0, 0, width1, height1, frame);
		}
		if (img[1] == null) {
			g.drawLine(400, 0, 800, 400);
		}
		else {
			g.drawImage(img[1], width1, 0, width2, height2, frame);
		}
	}
	
	public void run() {
		if (threadToRun == FIRST_WEBCAM) {
			threadToRun = SECOND_WEBCAM;
			Thread t = new Thread(this);
			t.start();
			while (running == true) {
				try {
					ii[FIRST_WEBCAM] = (ImageIcon)(streams[FIRST_WEBCAM].readObject());
					img[FIRST_WEBCAM] = (Image)(ii[0].getImage());
					width1 = img[FIRST_WEBCAM].getWidth(null);
					height1 = img[FIRST_WEBCAM].getHeight(null);
					//System.out.println("Got 1");
					frame.repaint();
				} catch (Exception ex) {
					ex.printStackTrace();
					System.exit(1);
				}
			}
		}
		else if (threadToRun == SECOND_WEBCAM){
			threadToRun = OUTPUT;
			Thread t = new Thread(this);
			t.start();
			while (running == true) {
				try {
					ii[SECOND_WEBCAM] = (ImageIcon)(streams[SECOND_WEBCAM].readObject());
					img[SECOND_WEBCAM] = (Image)(ii[1].getImage());
					width2 = img[SECOND_WEBCAM].getWidth(null);
					height2 = img[SECOND_WEBCAM].getHeight(null);
					//System.out.println("Got 2");
					frame.repaint();
				} catch (Exception ex) {
					ex.printStackTrace();
					System.exit(1);
				}
			}
		}
		else if (threadToRun == OUTPUT) {
			while (running == true) {
				try {
					if(scaleValue != (scaleBar.getValue()/100.0)){
						//System.out.println(scaleValue);
						scaleValue = scaleBar.getValue()/100.0;
						dos.writeDouble(scaleValue);
						dos.flush();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

}
