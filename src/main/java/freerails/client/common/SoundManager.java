package freerails.client.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;

import freerails.config.ClientConfig;
import freerails.controller.ModelRoot;

import org.apache.log4j.Logger;

/**
 * This class is responsible for loading and playing sounds. Samples are read
 * into a byte arrays so that they don't need to be loaded from disk each time
 * they are played.
 * 
 * @author Luke
 * 
 */
public class SoundManager implements ModelRootListener, LineListener {

    /**
     * Stores the audio data and properties of a sample.
     * 
     */
    private static class Sample {

        byte[] audio;

        AudioFormat format;

        DataLine.Info info;

        int size;
    }

    private static final Logger logger = Logger.getLogger(SoundManager.class
            .getName());

    private static final SoundManager soundManager = new SoundManager();

    public static SoundManager getSoundManager() {
        return soundManager;
    }

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

    private int maxLines;

    private Mixer mixer;

    private boolean playSounds = true;

    private HashMap<String, Sample> samples = new HashMap<String, Sample>();

    private final LinkedList<Clip> voices = new LinkedList<Clip>();

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

    public void propertyChange(ModelRoot.Property p, Object before, Object after) {
        if (p.equals(ModelRoot.Property.PLAY_SOUNDS)) {
            Boolean b = (Boolean) after;
            playSounds = b.booleanValue();
        }
    }

    public void update(LineEvent event) {
        // TODO free up resources when we have finished playing a clip.
    }

}