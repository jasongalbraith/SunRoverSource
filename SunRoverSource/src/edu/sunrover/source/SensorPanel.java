package edu.sunrover.source;

import java.awt.Graphics;
import javax.swing.JPanel;


public class SensorPanel extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int[] sensors;
	
	public SensorPanel(int[] sensorsIn) {
		sensors = sensorsIn;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawRect(50, 30, 60, 100);
		g.drawLine(80, 30, 80, 50);
		g.drawLine(80, 30, 85, 35);
		g.drawLine(80, 30, 75, 35);
		g.drawString(sensors[SunRoverSource.FORWARD]+" F", 80, 10);
		g.drawString(sensors[SunRoverSource.LEFT]+" L", 20, 90);
		g.drawString(sensors[SunRoverSource.RIGHT]+" R", 120, 90);
		g.drawString(sensors[SunRoverSource.BACKWARD]+" B", 80, 150);
		g.drawString(sensors[SunRoverSource.FORWARD_DOWN]+" FD", 80, 20);
		g.drawString(sensors[SunRoverSource.BACKWARD_DOWN]+" BD", 80, 160);
		g.drawString(sensors[SunRoverSource.COMPASS]+" COM", 60, 90);
	}
	
}
