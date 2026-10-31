package freerails.util.ui;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Tests sound manager behavior.
 */
public class SoundManagerTest extends TestCase {

    /**
     * Tests loads and caches sound clip behavior.
     * @throws Exception if the test loads and caches sound clip operation fails.
     */
    public void testLoadsAndCachesSoundClip() throws Exception {
        SoundManager manager = SoundManager.getInstance();
        String sound = "/freerails/client/sounds/cash.wav";

        manager.addClip(sound);
        manager.addClip(sound);
    }

    /**
     * Tests missing sound resource is reported behavior.
     */
    public void testMissingSoundResourceIsReported() {
        try {
            SoundManager.getInstance().addClip("/missing-test-sound.wav");
            fail("Expected IOException");
        } catch (IOException expected) {
        } catch (javax.sound.sampled.UnsupportedAudioFileException e) {
            fail("Unexpected audio format exception");
        }
    }

    /**
     * Tests disabled sound playback does not load or play clip behavior.
     */
    public void testDisabledSoundPlaybackDoesNotLoadOrPlayClip() {
        SoundManager manager = SoundManager.getInstance();
        manager.setPlayingSounds(false);

        try {
            assertFalse(manager.isPlayingSounds());
            manager.playSound("/missing-test-sound.wav", 0);
            assertFalse(manager.isPlayingSounds());
        } finally {
            manager.setPlayingSounds(true);
        }

        assertTrue(manager.isPlayingSounds());
    }
}
