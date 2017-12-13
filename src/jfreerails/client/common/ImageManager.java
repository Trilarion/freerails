/*
 * Created on 30-Apr-2003
 *
 */
package jfreerails.client.common;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * This interface defines methods for loading images.
 * @author Luke
 *
 */
public interface ImageManager {
    void setPathToReadFrom(String s);

    BufferedImage getImage(String relativeFilename) throws IOException;

    boolean contains(String relativeFilename);

    void setImage(String relativeFilename, BufferedImage i);

    BufferedImage getScaledImage(String relativeFilename, int height)
        throws IOException;
}
