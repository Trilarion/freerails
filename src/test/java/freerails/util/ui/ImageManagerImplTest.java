package freerails.util.ui;

import junit.framework.TestCase;

import java.awt.Image;
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
        assertTrue(ImageManagerImpl.isValid("folder/image.png"));
        try {
            manager.getScaledImage("image.png", 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }
}
