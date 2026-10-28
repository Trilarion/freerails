package freerails.util.ui;

import junit.framework.TestCase;

import java.awt.Image;
import java.awt.Graphics;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.BufferedImage;

public class ImageManagerImplTest extends TestCase {

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

    private void assertInvalidBlankImageDimensions(ImageManagerImpl manager, int height, int width) {
        try {
            manager.newBlankImage(height, width);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    private static final class ZeroSizedImage extends Image {
        @Override
        public int getWidth(ImageObserver observer) {
            return 0;
        }

        @Override
        public int getHeight(ImageObserver observer) {
            return 0;
        }

        @Override
        public ImageProducer getSource() {
            return null;
        }

        @Override
        public Graphics getGraphics() {
            return null;
        }

        @Override
        public Object getProperty(String name, ImageObserver observer) {
            return UndefinedProperty;
        }
    }
}
