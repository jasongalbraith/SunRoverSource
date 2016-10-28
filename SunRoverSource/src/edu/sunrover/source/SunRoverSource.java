package edu.sunrover.source;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class SunRoverSource implements Runnable, KeyListener {

	final static String IP_CONTROLS = "172.30.90.49";
	final static String IP_WEBCAM = "172.30.80.147";
	final static boolean CONTROLS_ENABLED = true;
	final static boolean WEBCAM_ENABLED = true;
	final static boolean AUDIO_ENABLED = true;
	Socket server;
	DataOutputStream dos;
	DataInputStream dis;
	Socket webcam1;
	ObjectInputStream ois1;
	Socket webcam2;
	ObjectInputStream ois2;
	DataOutputStream dos2;
	String ip;
	Controller c;
	long delta;
	long prevtime;
	long currenttime;
	long cd = 2000;
	boolean running = true;
	//Sensor array
	int[] sensors = new int[8];
	final static int COMPASS = 0;
	final static int FORWARD = 1;
	final static int BACKWARD = 2;
	final static int LEFT = 3;
	final static int RIGHT = 4;
	final static int FORWARD_DOWN = 5;
	final static int BACKWARD_DOWN = 6;
	final static int AI_STATE = 7;

	JFrame jf = new JFrame();
	JLabel TopLabel = new JLabel();
	JLabel MiddleLabel = new JLabel();
	JLabel BottomLabel = new JLabel();
	
	JSlider scaleBar = new JSlider(1,100);
	
	Container north = new Container();
	Container buttons = new Container();
	SensorPanel spanel = new SensorPanel(sensors);
	ImagePanel ipanel = new ImagePanel(jf, scaleBar);
	final int READ = 0;
	final int WRITE = 1;
	int threadToStart = READ;
	long startTime = 0;
	
	public SunRoverSource() {
		startTime = System.currentTimeMillis();
		jf.setSize(800,600);
		jf.setLayout(new GridLayout(2,1));
		buttons.setLayout(new GridLayout(4,1));
		scaleBar.setPaintLabels(true);
		buttons.add(TopLabel);
		buttons.add(MiddleLabel);
		buttons.add(BottomLabel);
		buttons.add(scaleBar);
		TopLabel.setText("Fine Drive");
		MiddleLabel.setText("Up-Forward, Down-Back");
		BottomLabel.setText("Left-Rotate CCW, Right-Rotate CW");
		north.setLayout(new GridLayout(1,2));
		north.add(buttons);
		spanel.setSize(200,200);
		north.add(spanel);
		jf.add(north);
		ipanel.setSize(800,400);
		jf.add(ipanel);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    
		jf.setVisible(true);
		
		System.out.println("Got this far");
		ControllerEnvironment ce = ControllerEnvironment
				.getDefaultEnvironment();
		System.out.println("Got environment");
		Controller[] cs = ce.getControllers();
		System.out.println("Got controller array");
		if (cs.length == 0) {
			System.out.println("No controllers found");
			System.exit(0);
		} else {
			System.out.println("No. of controllers: " + cs.length);
			for (int i = 0; i < cs.length; i++) {
				Controller.Type type = cs[i].getType();
				if ((type == Controller.Type.GAMEPAD)
						|| (type == Controller.Type.STICK)) {
					c = cs[i];
				}
			}
		}
		if (CONTROLS_ENABLED) {
			try {
				server = new Socket(IP_CONTROLS, 1234);
				dos = new DataOutputStream(new BufferedOutputStream(server.getOutputStream()));
				dis = new DataInputStream(new BufferedInputStream(server.getInputStream()));
			} catch (Exception e) {
				System.out.println("Could not connect to ip");
				System.exit(1);
			}

			Thread t = new Thread(this);
			t.setPriority(1);
			t.start();
		}
		/* Audio Creation */
		if (AUDIO_ENABLED) {
			try {
				// :)
				Socket audio = new Socket(IP_WEBCAM, 1234);
				SunRoverAudioOutput srao = new SunRoverAudioOutput(audio);
				SunRoverAudioInput srai = new SunRoverAudioInput(audio);
				ipanel.getSlider().addKeyListener(srai);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Could not connect to audio ip");
				System.exit(1);
			}	
		}
		/*Webcam Creation */
		if (WEBCAM_ENABLED) {
			try {
				webcam1 = new Socket(IP_WEBCAM, 1234);
				ois1 = new ObjectInputStream(new BufferedInputStream(webcam1.getInputStream()));
				ipanel.setInputStream(ois1, 0);
				dos2 = new DataOutputStream(new BufferedOutputStream(webcam1.getOutputStream()));
				ipanel.setOutputStream(dos2);
				webcam2 = new Socket(IP_WEBCAM, 1234);
				ois2 = new ObjectInputStream(new BufferedInputStream(webcam2.getInputStream()));
				ipanel.setInputStream(ois2, 1);
				Thread t = new Thread(ipanel);
				t.start();
			} catch (Exception e) {
				System.out.println("Could not connect to webcam2 ip");
				System.exit(1);
			}
		}
	}

	public void run() {
		try {	
			int value = 0;
			int previous = 0;
			if (threadToStart == READ) {
				threadToStart = WRITE;
				Thread t = new Thread(this);
				t.setPriority(9);
				t.start();
				long input = 0;
				while (running) {
					input = dis.readLong();
					if (input / 1000000000000l > 0) {
						sensors[FORWARD] = ((int)(input % 1000000000000l / 1000000000)); 
						sensors[BACKWARD] = ((int)(input % 1000000000 / 1000000));
						sensors[LEFT] = ((int)(input % 1000000 / 1000));
						sensors[RIGHT] = ((int)(input % 1000));
					}
					else {
						sensors[COMPASS] = ((int)(input / 1000000000));
						sensors[FORWARD_DOWN] = ((int)(input % 1000000000 / 1000000));
						sensors[BACKWARD_DOWN] = ((int)(input % 1000000 / 1000));
						sensors[AI_STATE] = ((int)(input % 1000));
					}
					jf.repaint();
				}
			}
			else {
				while (running) {
					c.poll();
					previous = value;
					value = 0;
					value += ((int) (c.getComponent(Component.Identifier.Axis.Y)
							.getPollData() * 100) + 255) * 1000;
					value += ((int) (c.getComponent(Component.Identifier.Axis.Z)
							.getPollData() * 100) + 255);
					value += ((int) (c.getComponent(Component.Identifier.Button._0)
							.getPollData())) * 1000000;
					if ((int) c.getComponent(Component.Identifier.Button._0).getPollData() == 1) {
						TopLabel.setText("Fine Drive");
						MiddleLabel.setText("Up-Forward, Down-Back");
						BottomLabel.setText("Left-Rotate CCW, Right-Rotate CW");
					}
					value += ((int) (c.getComponent(Component.Identifier.Button._1)
							.getPollData()) * 2) * 1000000;
					if ((int) c.getComponent(Component.Identifier.Button._1).getPollData() == 1) {
						TopLabel.setText("WebCam");
						MiddleLabel.setText("Up-Cam Up, Down-Cam Down");
						BottomLabel.setText("Left-Rotate CCW, Right-Rotate CW");
					}
					value += ((int) (c.getComponent(Component.Identifier.Button._2)
							.getPollData()) * 4) * 1000000;
					if ((int) c.getComponent(Component.Identifier.Button._2).getPollData() == 1) {
						TopLabel.setText("Arm");
						MiddleLabel.setText("Up-Arm up, Down-Arm down");
						BottomLabel.setText("Left-Arm CCW, Right-Arm CW");
					}
					value += ((int) (c.getComponent(Component.Identifier.Button._3)
							.getPollData()) * 8) * 1000000;
					if ((int) c.getComponent(Component.Identifier.Button._3).getPollData() == 1) {
						TopLabel.setText("Claw");
						MiddleLabel.setText("Up-Nothing, Down-Nothing");
						BottomLabel.setText("Left-Pull in, Right-Push out");
					}
					value += (int) (c.getComponent(Component.Identifier.Button._4)
							.getPollData() * 16) * 1000000;
					if ((int) c.getComponent(Component.Identifier.Button._4).getPollData() == 1) {
						TopLabel.setText("Drive");
						MiddleLabel.setText("Up-Forward, Down-Back");
						BottomLabel.setText("Left-Rotate CCW, Right-Rotate CW");
					}
					value += ((int) (c.getComponent(Component.Identifier.Button._8)
							.getPollData()) * 32) * 1000000;
					value += ((int) (c.getComponent(Component.Identifier.Button._9)
							.getPollData()) * 64) * 1000000;
					if ((int) c.getComponent(Component.Identifier.Button._9).getPollData() == 1) {
						TopLabel.setText("AutoPilot!");
						MiddleLabel.setText("");
						BottomLabel.setText(""+sensors[AI_STATE]);
					}
					if (value != previous){
						System.out.println("Sent:"+value+":"+(System.currentTimeMillis()-startTime));
						dos.writeInt(value);
						dos.flush();
					}
				}
			}
			dis.close();
			dos.close();
			server.close();
			System.exit(0);
		}catch (Exception ex){
			try {
				running = false;
				TopLabel.setText("Lost Connection...");
				MiddleLabel.setText("");
				BottomLabel.setText("");
				System.out.println("Lost connection...trying again.");
				server = new Socket(IP_CONTROLS, 1234);
				dos = new DataOutputStream(new BufferedOutputStream(server.getOutputStream()));
				dis = new DataInputStream(new BufferedInputStream(server.getInputStream()));
				System.out.println("Reconnecting...");
				TopLabel.setText("Drive");
				MiddleLabel.setText("Up-Forward, Down-Back");
				BottomLabel.setText("Left-Rotate CCW, Right-Rotate CW");
				dos.writeInt(1255255);
				dos.flush();
				running = true;
				threadToStart = READ;
				Thread t = new Thread(this);
				t.setPriority(1);
				t.start();
			}
			catch (Exception ex2){
				ex2.printStackTrace();
				System.exit(0);
			}
		}
		
	}

	public static void main(String[] args) {
		new SunRoverSource();
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		System.out.println("Key pressed");
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		System.out.println("Key released");
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		System.out.println("Key typed");
	}

}
