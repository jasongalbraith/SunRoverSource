package edu.sunrover.source;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ImageInputStream extends ObjectInputStream {

	protected ImageInputStream() throws IOException, SecurityException {
		super();
	}
	
	protected ImageInputStream(BufferedInputStream br) throws IOException {
		super(br);
	}
	
	protected boolean enableResolveObject(boolean enable) throws SecurityException {
		return super.enableResolveObject(enable);
	}
	
	protected Object readObjectOverride() throws ClassNotFoundException, IOException{
		return super.readObjectOverride();
	}

}
