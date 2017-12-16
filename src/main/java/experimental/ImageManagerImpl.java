/*
 * Created on 30-Apr-2003
 *
 */
package experimental;

import freerails.client.common.ImageManager;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of ImageManager that returns images that are compatible with
 * the current graphics configuration and whose transparency is set to
 * TRANSLUCENT, the scaled images it returns are rendered with renderingHints
 * set for quality.
 *
 * @author Luke
 */
public class ImageManagerImpl implements ImageManager {
    /**
     * Matches anying but a string beginning with a "/"*. The reason for this
     * check is that relative filenames such as "/cursor/removetrack.png" work
     * from with files but not from within jars, which lets bugs slip in.
     */
    private static final String A_REGEX = "^[^///].*";

    private static final Logger logger = Logger
            .getLogger(ImageManagerImpl.class.getName());

    private static final Pattern pattern = Pattern.compile(A_REGEX);

    public static boolean isValid(String s) {
        Matcher m = pattern.matcher(s);
        return m.matches();
    }

    private final GraphicsConfiguration defaultConfiguration = GraphicsEnvironment
            .getLocalGraphicsEnvironment().getDefaultScreenDevice()
            .getDefaultConfiguration();

    private final HashMap<String, Image> imageHashMap = new HashMap<String, Image>();

    private String pathToReadFrom;

    private String pathToWriteTo;

    private final RenderingHints renderingHints;

    private final HashMap<String, Image> scaledImagesHashMap = new HashMap<String, Image>();

    public ImageManagerImpl(String readpath) {
        this(readpath, null);
    }

    public ImageManagerImpl(String readpath, String writePath) {
        pathToReadFrom = readpath;
        pathToWriteTo = writePath;
        // Attempt to increase quality..
        renderingHints = new RenderingHints(
                RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        renderingHints.put(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        renderingHints.put(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    public boolean contains(String relativeFilename) {
        relativeFilename = relativeFilename.replace(' ', '_');

        if (imageHashMap.containsKey(relativeFilename)) {
            return true;
        }
        File f = new File(pathToWriteTo + File.separator + relativeFilename);

        if (f.isFile()) {
            return true;
        }
        return false;
    }

    public Image getImage(String relativeFilename) throws IOException {
        String hashKey = relativeFilename;
        Image compatibleImage = imageHashMap.get(hashKey);

        if (compatibleImage != null) {
            return compatibleImage;
        }
        relativeFilename = relativeFilename.replace(' ', '_');
        if (!isValid(relativeFilename))
            throw new IllegalArgumentException(relativeFilename
                    + " must match " + A_REGEX);

        // File f = new File(pathToReadFrom+File.separator+relativeFilename);
        String read = pathToReadFrom + relativeFilename;
        read = read.replace(File.separatorChar, '/');

        URL url = ImageManagerImpl.class.getResource(read);

        if (null == url) {
            throw new IOException("Couldn't find: " + read);
        }

        Image tempImage = ImageIO.read(url);
        if (null == tempImage) {
            throw new IOException("Couldn't find: " + read);
        }
        compatibleImage = defaultConfiguration.createCompatibleImage(tempImage
                        .getWidth(null), tempImage.getHeight(null),
                Transparency.TRANSLUCENT);
        Graphics g = compatibleImage.getGraphics();
        g.drawImage(tempImage, 0, 0, null);
        imageHashMap.put(hashKey, compatibleImage);

        return compatibleImage;
    }

    /**
     * Returns the specified image scaled so that its height is equal to the
     * specified height.
     */
    public Image getScaledImage(String relativeFilename, int height)
            throws IOException {
        String hashKey = relativeFilename + height;
        Image compatibleImage = scaledImagesHashMap.get(hashKey);
        if (compatibleImage != null) {
            return compatibleImage;
        }

        relativeFilename = relativeFilename.replace(' ', '_');
        if (!isValid(relativeFilename))
            throw new IllegalArgumentException(relativeFilename
                    + " must match " + A_REGEX);

        Image i = getImage(relativeFilename);
        if (i.getHeight(null) == height) {
            scaledImagesHashMap.put(hashKey, i);
            return i;
        }
        int width = (i.getWidth(null) * height) / i.getHeight(null);
        compatibleImage = newBlankImage(height, width);
        Graphics2D g = (Graphics2D) compatibleImage.getGraphics();
        g.setRenderingHints(this.renderingHints);
        g.drawImage(i, 0, 0, width, height, null);
        scaledImagesHashMap.put(hashKey, compatibleImage);

        return compatibleImage;
    }

    public Image newBlankImage(int height, int width) {
        Image compatibleImage = defaultConfiguration.createCompatibleImage(
                width, height, Transparency.TRANSLUCENT);
        return compatibleImage;
    }

    public void setImage(String relativeFilename, Image i) {
        relativeFilename = relativeFilename.replace(' ', '_');

        if (i == null) {
            throw new NullPointerException(relativeFilename);
        }

        imageHashMap.put(relativeFilename, i);
    }

    public void setPathToReadFrom(String s) {
        pathToReadFrom = s;
    }

    public void setPathToWriteTo(String s) {
        pathToWriteTo = s;
    }

    public void writeAllImages() throws IOException {

        for (String s : imageHashMap.keySet()) {
            writeImage(s);
        }
    }

    public void writeImage(String relativeFilename) throws IOException {

        if (null == pathToWriteTo)
            throw new NullPointerException("null == pathToWriteTo");

        relativeFilename = relativeFilename.replace(' ', '_');

        File f = new File(pathToWriteTo + File.separator + relativeFilename);

        if (imageHashMap.containsKey(relativeFilename)) {
            RenderedImage i = (RenderedImage) imageHashMap
                    .get(relativeFilename);
            String pathName = f.getPath();
            File path = new File(pathName);
            path.mkdirs();

            ImageIO.write(i, "png", f);
            logger.info("Writing " + f);
        } else {
            throw new NoSuchElementException(relativeFilename);
        }
    }
}