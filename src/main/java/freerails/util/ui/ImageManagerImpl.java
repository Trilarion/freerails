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
package freerails.util.ui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of ImageManager that returns images that are compatible with
 * the current graphics configuration and whose transparency is set to
 * TRANSLUCENT, the scaled images it returns are rendered with renderingHints
 * set for quality.
 */

public class ImageManagerImpl implements ImageManager {
    private final GraphicsConfiguration defaultConfiguration;
    private final Map<String, Image> imageHashMap = new HashMap<>();
    private final RenderingHints renderingHints;
    private final Map<ScaledImageKey, Image> scaledImagesHashMap = new HashMap<>();
    private final String pathToReadFrom;

    /**
     * @param readPath
     */
    public ImageManagerImpl(String readPath) {
        pathToReadFrom = Objects.requireNonNull(readPath, "readPath");
        defaultConfiguration = GraphicsEnvironment.isHeadless()
                ? null
                : GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();
        // Attempt to increase quality..
        renderingHints = new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    /**
     * @param s
     * @return
     */
    public static boolean isValid(CharSequence s) {
        return s != null && !s.isEmpty() && s.charAt(0) != '/'
                && s.charAt(0) != '\\' && s.toString().indexOf('\0') < 0
                && !hasParentDirectory(s);
    }

    private static boolean hasParentDirectory(CharSequence path) {
        String normalized = path.toString().replace('\\', '/');
        for (String part : normalized.split("/")) {
            if (part.equals("..")) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeFilename(String filename) {
        Objects.requireNonNull(filename, "relativeFilename");
        String normalized = filename.replace(' ', '_').replace('\\', '/');
        if (!isValid(normalized)) {
            throw new IllegalArgumentException(normalized + " must be a relative resource path.");
        }
        return normalized;
    }

    /**
     * @param relativeFilename
     * @return
     * @throws IOException
     */
    @Override
    public Image getImage(String relativeFilename) throws IOException {
        String hashKey = normalizeFilename(relativeFilename);
        Image compatibleImage = imageHashMap.get(hashKey);
        if (compatibleImage != null) {
            return compatibleImage;
        }

        String read = pathToReadFrom + hashKey;
        read = read.replace(File.separatorChar, '/');

        URL url = ImageManagerImpl.class.getResource(read);

        if (null == url) {
            throw new IOException("Couldn't find: " + read);
        }

        Image tempImage = ImageIO.read(url);
        if (null == tempImage) {
            throw new IOException("Unsupported or unreadable image: " + read);
        }
        if (defaultConfiguration == null) {
            compatibleImage = new BufferedImage(tempImage.getWidth(null), tempImage.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB);
        } else {
            compatibleImage = defaultConfiguration.createCompatibleImage(tempImage.getWidth(null),
                    tempImage.getHeight(null), Transparency.TRANSLUCENT);
        }
        Graphics2D g = (Graphics2D) compatibleImage.getGraphics();
        try {
            g.drawImage(tempImage, 0, 0, null);
        } finally {
            g.dispose();
        }
        imageHashMap.put(hashKey, compatibleImage);

        return compatibleImage;
    }

    /**
     * Returns the specified image scaled so that its height is equal to the
     * specified height.
     */
    @Override
    public Image getScaledImage(String relativeFilename, int height) throws IOException {
        if (height <= 0) {
            throw new IllegalArgumentException("height must be positive: " + height);
        }
        String normalizedFilename = normalizeFilename(relativeFilename);
        ScaledImageKey hashKey = new ScaledImageKey(normalizedFilename, height);
        Image compatibleImage = scaledImagesHashMap.get(hashKey);
        if (compatibleImage != null) {
            return compatibleImage;
        }

        Image i = getImage(normalizedFilename);
        if (i.getHeight(null) == height) {
            scaledImagesHashMap.put(hashKey, i);
            return i;
        }
        int width = Math.max(1, (int) ((long) i.getWidth(null) * height / i.getHeight(null)));
        compatibleImage = newBlankImage(height, width);
        Graphics2D g = (Graphics2D) compatibleImage.getGraphics();
        try {
            g.setRenderingHints(renderingHints);
            g.drawImage(i, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        scaledImagesHashMap.put(hashKey, compatibleImage);

        return compatibleImage;
    }

    /**
     * @param height
     * @param width
     * @return
     */
    @Override
    public Image newBlankImage(int height, int width) {
        if (height <= 0 || width <= 0) {
            throw new IllegalArgumentException("Image dimensions must be positive.");
        }
        return defaultConfiguration == null
                ? new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                : defaultConfiguration.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
    }

    /**
     * @param relativeFilename
     * @param i
     */
    @Override
    public void setImage(String relativeFilename, Image i) {
        String key = normalizeFilename(relativeFilename);
        Image image = Objects.requireNonNull(i, "image");
        if (image.getWidth(null) <= 0 || image.getHeight(null) <= 0) {
            throw new IllegalArgumentException("Image must have positive dimensions.");
        }
        imageHashMap.put(key, image);
        scaledImagesHashMap.keySet().removeIf(scaledKey -> scaledKey.filename.equals(key));
    }

    private record ScaledImageKey(String filename, int height) {
    }
}