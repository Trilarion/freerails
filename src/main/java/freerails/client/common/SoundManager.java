package freerails.client.common;

import freerails.client.ClientConfig;
import freerails.controller.ModelRoot;
import org.apache.log4j.Logger;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class is responsible for loading and playing sounds. Samples are read
 * into a byte arrays so that they don't need to be loaded from disk each time
 * they are played.
 *
 */
public class SoundManager implements ModelRootListener, LineListener {

    private static final Logger logger = Logger.getLogger(SoundManager.class
            .getName());
    private static final SoundManager soundManager = new SoundManager();
    private final HashMap<String, Sample> samples = new HashMap<>();
    private final LinkedList<Clip> voices = new LinkedList<>();
    private int maxLines;
    private Mixer mixer;
    private boolean playSounds = true;

    private SoundManager() {
        AudioFormat format2 = new AudioFormat(8000f, 16, 1, true, false);
        DataLine.Info info2 = new DataLine.Info(null, format2, 0);
        for (Mixer.Info mo : AudioSystem.getMixerInfo()) {

            mixer = AudioSystem.getMixer(mo);
            maxLines = mixer.getMaxLines(info2);
            if (maxLines >= 32)
                break; // Java Sound Audio Engine, version 1.0 satisfies this.
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Sound Mixer: " + mixer.getMixerInfo() + "("
                    + maxLines + " voices).");
        }

    }

    /**
     *
     * @return
     */
    public static SoundManager getSoundManager() {
        return soundManager;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {

        SoundManager soundPlayer = getSoundManager();
        for (int i = 0; i < 100; i++) {
            soundPlayer.playSound(ClientConfig.SOUND_CASH, 10);

            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param s
     * @throws IOException
     * @throws UnsupportedAudioFileException
     * @throws LineUnavailableException
     */
    public void addClip(String s) throws IOException,
            UnsupportedAudioFileException, LineUnavailableException {
        if (samples.containsKey(s)) {
            return;
        }

        URL url = getClass().getResource(s);
        AudioInputStream audioInputStream = AudioSystem
                .getAudioInputStream(loadStream(url.openStream()));

        Sample sample = new Sample();

        sample.format = audioInputStream.getFormat();
        sample.size = (int) (sample.format.getFrameSize() * audioInputStream
                .getFrameLength());
        sample.audio = new byte[sample.size];
        sample.info = new DataLine.Info(Clip.class, sample.format, sample.size);
        audioInputStream.read(sample.audio, 0, sample.size);
        samples.put(s, sample);
    }

    private ByteArrayInputStream loadStream(InputStream inputstream)
            throws IOException {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];

        for (int i = inputstream.read(data); i != -1; i = inputstream
                .read(data)) {
            bytearrayoutputstream.write(data, 0, i);
        }

        inputstream.close();
        bytearrayoutputstream.close();
        data = bytearrayoutputstream.toByteArray();

        return new ByteArrayInputStream(data);
    }

    /**
     *
     * @param s
     * @param loops
     */
    public void playSound(String s, int loops) {
        if (playSounds) {
            try {
                if (!samples.containsKey(s)) {
                    addClip(s);
                }

                Sample sample = samples.get(s);

                Clip clip;
                if (voices.size() < maxLines) {
                    clip = (Clip) mixer.getLine(sample.info);
                } else {
                    clip = voices.removeFirst();
                    clip.stop();
                    clip.flush();
                    clip.close();
                }
                clip.addLineListener(this);
                clip.open(sample.format, sample.audio, 0, sample.size);
                clip.loop(loops);
                voices.add(clip);
            } catch (LineUnavailableException e) {
                logger.warn(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param p
     * @param before
     * @param after
     */
    public void propertyChange(ModelRoot.Property p, Object before, Object after) {
        if (p.equals(ModelRoot.Property.PLAY_SOUNDS)) {
            playSounds = (Boolean) after;
        }
    }

    public void update(LineEvent event) {
        // TODO free up resources when we have finished playing a clip.
    }

    /**
     * Stores the audio data and properties of a sample.
     */
    private static class Sample {

        byte[] audio;

        AudioFormat format;

        DataLine.Info info;

        int size;
    }

}