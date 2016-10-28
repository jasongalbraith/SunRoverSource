package edu.sunrover.source;

import java.awt.image.BufferedImage;
import java.io.Serializable;


public class RoverImage extends BufferedImage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RoverImage() {
		this(0,0,0);
	}
	
	public RoverImage(int width, int height, int imageType) {
		super(width, height, imageType);
	}
	
	public RoverImage(BufferedImage bimg) {
		this(bimg.getWidth(), bimg.getHeight(), bimg.getType());
	}

}
