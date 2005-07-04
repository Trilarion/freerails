/*
 * Created on 30-Apr-2003
 *
 */
package jfreerails.client.common;

import java.awt.Image;
import java.io.IOException;

/**
 * This interface defines methods for loading and saving images, and producing
 * scaled images whose quality may be controlled.
 * 
 * @author Luke
 * 
 */
public interface ImageManager {

	Image newBlankImage(int height, int width);

	void setPathToReadFrom(String s);

	void setPathToWriteTo(String s);

	Image getImage(String relativeFilename) throws IOException;

	boolean contains(String relativeFilename);

	void setImage(String relativeFilename, Image i);

	void writeImage(String relativeFilename) throws IOException;

	void writeAllImages() throws IOException;

	Image getScaledImage(String relativeFilename, int height)
			throws IOException;
}