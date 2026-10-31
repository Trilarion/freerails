package freerails.util.ui;

import junit.framework.TestCase;

import java.awt.Image;
import java.awt.Graphics;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.BufferedImage;

/**
 * Tests image manager impl behavior.
 */
public class ImageManagerImplTest extends TestCase {

    /**
     * Tests normalized cache and replacement invalidation behavior.
     * @throws Exception if the test normalized cache and replacement invalidation operation fails.
     */
    public void testNormalizedCacheAndReplacementInvalidation() throws Exception {
        ImageManagerImpl manager = new ImageManagerImpl("");
        Image first = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        Image replacement = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);

        manager.setImage("a picture.png", first);
        assertSame(first, manager.getImage("a picture.png"));
        assertSame(first, manager.getScaledImage("a picture.png", 4));

        manager.setImage("a_picture.png", replacement);

        assertSame(replacement, manager.getImage("a picture.png"));
        assertSame(replacement, manager.getScaledImage("a picture.png", 2));
    }

    /**
     * Tests rejects parent directory and invalid scale behavior.
     * @throws Exception if the test rejects parent directory and invalid scale operation fails.
     */
    public void testRejectsParentDirectoryAndInvalidScale() throws Exception {
        ImageManagerImpl manager = new ImageManagerImpl("");

        assertFalse(ImageManagerImpl.isValid("../image.png"));
        assertFalse(ImageManagerImpl.isValid("folder/../image.png"));
        assertFalse(ImageManagerImpl.isValid(""));
        assertFalse(ImageManagerImpl.isValid(null));
        assertFalse(ImageManagerImpl.isValid("/image.png"));
        assertFalse(ImageManagerImpl.isValid("\\image.png"));
        assertFalse(ImageManagerImpl.isValid("image\u0000.png"));
        assertTrue(ImageManagerImpl.isValid("folder/image.png"));
        try {
            manager.getScaledImage("image.png", 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    /**
     * Tests loads and caches image and scaled variants behavior.
     * @throws Exception if the test loads and caches image and scaled variants operation fails.
     */
    public void testLoadsAndCachesImageAndScaledVariants() throws Exception {
        ImageManagerImpl manager = new ImageManagerImpl("/freerails/client/graphics/");

        Image image = manager.getImage("icons/error.gif");

        assertTrue(image.getWidth(null) > 0);
        assertTrue(image.getHeight(null) > 0);
        assertSame(image, manager.getImage("icons/error.gif"));
        assertSame(image, manager.getScaledImage("icons/error.gif", image.getHeight(null)));

        int scaledHeight = image.getHeight(null) * 2;
        Image scaled = manager.getScaledImage("icons/error.gif", scaledHeight);
        assertEquals(scaledHeight, scaled.getHeight(null));
        assertEquals(image.getWidth(null) * 2, scaled.getWidth(null));
        assertSame(scaled, manager.getScaledImage("icons/error.gif", scaledHeight));
    }

    /**
     * Tests rejects missing images and invalid image arguments behavior.
     * @throws Exception if the test rejects missing images and invalid image arguments operation fails.
     */
    public void testRejectsMissingImagesAndInvalidImageArguments() throws Exception {
        ImageManagerImpl manager = new ImageManagerImpl("/freerails/client/graphics/");

        try {
            manager.getImage("missing.gif");
            fail("Expected IOException");
        } catch (java.io.IOException expected) {
            // expected
        }
        assertInvalidImagePath(manager, "../icons/error.gif");
        try {
            manager.getImage(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }
        assertInvalidBlankImageDimensions(manager, 0, 1);
        assertInvalidBlankImageDimensions(manager, 1, 0);
        try {
            manager.setImage("icons/error.gif", null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }
        try {
            manager.setImage("icons/error.gif", new ZeroSizedImage());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    /**
     * Asserts invalid image path.
     * @param manager the manager to use in the test.
     * @param path the path to use in the test.
     */
    private void assertInvalidImagePath(ImageManagerImpl manager, String path) {
        try {
            manager.getImage(path);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        } catch (java.io.IOException e) {
            fail("Unexpected IOException");
        }
    }

    /**
     * Asserts invalid blank image dimensions.
     * @param manager the manager to use in the test.
     * @param height the height to use in the test.
     * @param width the width to use in the test.
     */
    private void assertInvalidBlankImageDimensions(ImageManagerImpl manager, int height, int width) {
        try {
            manager.newBlankImage(height, width);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    /**
     * Provides test support for zero sized image.
     */
    private static final class ZeroSizedImage extends Image {
        /**
         * Provides test support for zero sized image.
         */
        @Override
        public int getWidth(ImageObserver observer) {
            return 0;
        }

        /**
         * Returns height.
         * @param observer the observer used by this test.
         * @return the result of get height.
         */
        @Override
        public int getHeight(ImageObserver observer) {
            return 0;
        }

        /**
         * Returns source.
         * @return the result of get source.
         */
        @Override
        public ImageProducer getSource() {
            return null;
        }

        /**
         * Returns graphics.
         * @return the result of get graphics.
         */
        @Override
        public Graphics getGraphics() {
            return null;
        }

        /**
         * Returns property.
         * @param name the name used by this test.
         * @param observer the observer used by this test.
         * @return the result of get property.
         */
        @Override
        public Object getProperty(String name, ImageObserver observer) {
            return UndefinedProperty;
        }
    }
}
