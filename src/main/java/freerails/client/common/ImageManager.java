/*
 * Created on 30-Apr-2003
 *
 */
package freerails.client.common;

import java.awt.*;
import java.io.IOException;

/**
 * This interface defines methods for loading and producing
 * scaled images whose quality may be controlled.
 *
 * @author Luke
 */
public interface ImageManager {

    Image newBlankImage(int height, int width);

    void setPathToReadFrom(String s);

    Image getImage(String relativeFilename) throws IOException;

    boolean contains(String relativeFilename);

    void setImage(String relativeFilename, Image i);

    Image getScaledImage(String relativeFilename, int height)
            throws IOException;
}