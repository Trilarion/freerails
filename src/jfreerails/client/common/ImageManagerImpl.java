/*
 * Created on 30-Apr-2003
 *
 */
package jfreerails.client.common;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.imageio.ImageIO;


/**
 * @author Luke
 *
 */
public class ImageManagerImpl implements ImageManager {
    private String pathToReadFrom;
    private String pathToWriteTo;
    private HashMap imageHashMap = new HashMap();
    private HashMap scaledImagesHashMap = new HashMap();
    private GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                                            .getDefaultScreenDevice()
                                                                            .getDefaultConfiguration();

    public ImageManagerImpl(String readpath, String writePath) {
        pathToReadFrom = readpath;
        pathToWriteTo = writePath;
    }

    public void setPathToReadFrom(String s) {
        pathToReadFrom = s;
    }

    public void setPathToWriteTo(String s) {
        pathToWriteTo = s;
    }

    public Image getImage(String relativeFilename) throws IOException {
        relativeFilename = relativeFilename.replace(' ', '_');

        if (imageHashMap.containsKey(relativeFilename)) {
            return (Image)imageHashMap.get(relativeFilename);
        }

        //File f = new File(pathToReadFrom+File.separator+relativeFilename);
        String read = pathToReadFrom + relativeFilename;
        read = read.replace(File.separatorChar, '/');

        URL url = ImageManagerImpl.class.getResource(read);

        if (null == url) {
            throw new IOException("Couldn't find: " + read);
        }

        Image tempImage = ImageIO.read(url);
        Image compatibleImage = defaultConfiguration.createCompatibleImage(tempImage.getWidth(
                    null), tempImage.getHeight(null), Transparency.TRANSLUCENT);
        Graphics g = compatibleImage.getGraphics();
        g.drawImage(tempImage, 0, 0, null);
        imageHashMap.put(relativeFilename, compatibleImage);

        return compatibleImage;
    }

    public boolean contains(String relativeFilename) {
        relativeFilename = relativeFilename.replace(' ', '_');

        if (imageHashMap.containsKey(relativeFilename)) {
            return true;
        } else {
            File f = new File(pathToWriteTo + File.separator +
                    relativeFilename);

            if (f.isFile()) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void setImage(String relativeFilename, Image i) {
        relativeFilename = relativeFilename.replace(' ', '_');

        if (i == null) {
            throw new NullPointerException(relativeFilename);
        }

        imageHashMap.put(relativeFilename, i);
    }

    public void writeImage(String relativeFilename) throws IOException {
        relativeFilename = relativeFilename.replace(' ', '_');

        File f = new File(pathToWriteTo + File.separator + relativeFilename);

        if (imageHashMap.containsKey(relativeFilename)) {
            RenderedImage i = (RenderedImage)imageHashMap.get(relativeFilename);
            String pathName = f.getPath();
            File path = new File(pathName);
            path.mkdirs();

            ImageIO.write(i, "png", f);
        } else {
            throw new NoSuchElementException(relativeFilename);
        }
    }

    public void writeAllImages() throws IOException {
        Iterator it = imageHashMap.keySet().iterator();

        while (it.hasNext()) {
            String s = (String)it.next();
            writeImage(s);
        }
    }

    /** Returns the specified image scaled so that its height is equal to the specified height. */
    public Image getScaledImage(String relativeFilename, int height)
        throws IOException {
        Image i = getImage(relativeFilename);
        String hashKey = relativeFilename + height;

        if (this.scaledImagesHashMap.containsKey(hashKey)) {
            return (Image)scaledImagesHashMap.get(hashKey);
        } else {
            if (i.getHeight(null) == height) {
                return i;
            } else {
                int width = (i.getWidth(null) * height) / i.getHeight(null);
                Image compatibleImage = defaultConfiguration.createCompatibleImage(width,
                        height, Transparency.BITMASK);
                Graphics g = compatibleImage.getGraphics();
                g.drawImage(i, 0, 0, width, height, null);
                scaledImagesHashMap.put(hashKey, compatibleImage);

                return compatibleImage;
            }
        }
    }
}