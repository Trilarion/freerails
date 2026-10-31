/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *
 */
package freerails;

import freerails.util.ui.ImageManager;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of ImageManager that returns images that are compatible with
 * the current graphics configuration and whose transparency is set to
 * TRANSLUCENT, the scaled images it returns are rendered with renderingHints
 * set for quality.
 */
public class ImageManagerImpl implements ImageManager {
    /**
     * Matches anything but a string beginning with a "/"*. The reason for this
     * check is that relative file names such as "/cursor/removetrack.png" work
     * from with files but not from within jars, which lets bugs slip in.
     */
    private static final String A_REGEX = "^[^///].*";
    private static final Pattern pattern = Pattern.compile(A_REGEX);
    private final GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    private final HashMap<String, Image> imageHashMap = new HashMap<>();
    private final RenderingHints renderingHints;
    private final Map<String, Image> scaledImagesHashMap = new HashMap<>();
    private String pathToReadFrom;
    private String pathToWriteTo;

    /**
     * Creates an image manager that reads from the supplied classpath path.
     *
     * @param readpath the classpath prefix used to load images
     */
    private ImageManagerImpl(String readpath) {
        this(readpath, null);
    }

    /**
     * Creates an image manager with paths for reading and writing images.
     *
     * @param readpath the classpath prefix used to load images
     * @param writePath the directory used to write images, or {@code null} if
     *        writing is disabled
     */
    public ImageManagerImpl(String readpath, String writePath) {
        pathToReadFrom = readpath;
        pathToWriteTo = writePath;
        // Attempt to increase quality..
        renderingHints = new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    /**
     * Checks whether a relative image path is valid.
     *
     * @param s the path to validate
     * @return {@code true} if the path does not begin with a slash
     */
    private static boolean isValid(CharSequence s) {
        Matcher m = pattern.matcher(s);
        return m.matches();
    }

    /**
     * Loads an image from the configured classpath directory.
     *
     * @param relativeFilename the image's relative filename
     * @return the loaded image
     * @throws IOException if the image cannot be found or read
     */
    @Override
    public Image getImage(String relativeFilename) throws IOException {
        String hashKey = relativeFilename;
        Image compatibleImage = imageHashMap.get(hashKey);

        if (compatibleImage != null) {
            return compatibleImage;
        }
        relativeFilename = relativeFilename.replace(' ', '_');
        if (!isValid(relativeFilename)) throw new IllegalArgumentException(relativeFilename + " must match " + A_REGEX);

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
        compatibleImage = defaultConfiguration.createCompatibleImage(tempImage.getWidth(null), tempImage.getHeight(null), Transparency.TRANSLUCENT);
        Graphics g = compatibleImage.getGraphics();
        g.drawImage(tempImage, 0, 0, null);
        imageHashMap.put(hashKey, compatibleImage);

        return compatibleImage;
    }

    /**
     * Returns the specified image scaled so that its height is equal to the
     * specified height.
     *
     * @param relativeFilename the image's relative filename
     * @param height the requested image height
     * @return the scaled image
     * @throws IOException if the image cannot be loaded
     */
    @Override
    public Image getScaledImage(String relativeFilename, int height) throws IOException {
        String hashKey = relativeFilename + height;
        Image compatibleImage = scaledImagesHashMap.get(hashKey);
        if (compatibleImage != null) {
            return compatibleImage;
        }

        relativeFilename = relativeFilename.replace(' ', '_');
        if (!isValid(relativeFilename)) throw new IllegalArgumentException(relativeFilename + " must match " + A_REGEX);

        Image i = getImage(relativeFilename);
        if (i.getHeight(null) == height) {
            scaledImagesHashMap.put(hashKey, i);
            return i;
        }
        int width = i.getWidth(null) * height / i.getHeight(null);
        compatibleImage = newBlankImage(height, width);
        Graphics2D g = (Graphics2D) compatibleImage.getGraphics();
        g.setRenderingHints(renderingHints);
        g.drawImage(i, 0, 0, width, height, null);
        scaledImagesHashMap.put(hashKey, compatibleImage);

        return compatibleImage;
    }

    /**
     * Creates a transparent image with the specified dimensions.
     *
     * @param height the image height
     * @param width the image width
     * @return the new compatible image
     */
    @Override
    public Image newBlankImage(int height, int width) {
        return defaultConfiguration.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
    }

    /**
     * Stores an image under the supplied relative filename.
     *
     * @param relativeFilename the image's relative filename
     * @param i the image to store
     */
    @Override
    public void setImage(String relativeFilename, Image i) {
        relativeFilename = relativeFilename.replace(' ', '_');

        if (i == null) {
            throw new NullPointerException(relativeFilename);
        }

        imageHashMap.put(relativeFilename, i);
    }

    /**
     * Writes all cached images to the configured output directory.
     *
     * @throws IOException if an image cannot be written
     */
    public void writeAllImages() throws IOException {

        for (String s : imageHashMap.keySet()) {
            writeImage(s);
        }
    }

    /**
     * Writes a cached image to the configured output directory.
     *
     * @param relativeFilename the image's relative filename
     * @throws IOException if the image cannot be written
     */
    private void writeImage(String relativeFilename) throws IOException {

        if (null == pathToWriteTo) throw new NullPointerException("null == pathToWriteTo");

        relativeFilename = relativeFilename.replace(' ', '_');

        File f = new File(pathToWriteTo + File.separator + relativeFilename);

        if (imageHashMap.containsKey(relativeFilename)) {
            RenderedImage i = (RenderedImage) imageHashMap.get(relativeFilename);
            String pathName = f.getPath();
            File path = new File(pathName);
            path.mkdirs();

            ImageIO.write(i, "png", f);
            System.out.println("Writing " + f);
        } else {
            throw new NoSuchElementException(relativeFilename);
        }
    }
}