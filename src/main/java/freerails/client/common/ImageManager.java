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
 */
public interface ImageManager {

    /**
     *
     * @param height
     * @param width
     * @return
     */
    Image newBlankImage(int height, int width);

    /**
     *
     * @param s
     */
    void setPathToReadFrom(String s);

    /**
     *
     * @param relativeFilename
     * @return
     * @throws IOException
     */
    Image getImage(String relativeFilename) throws IOException;

    /**
     *
     * @param relativeFilename
     * @return
     */
    boolean contains(String relativeFilename);

    /**
     *
     * @param relativeFilename
     * @param i
     */
    void setImage(String relativeFilename, Image i);

    /**
     *
     * @param relativeFilename
     * @param height
     * @return
     * @throws IOException
     */
    Image getScaledImage(String relativeFilename, int height)
            throws IOException;
}