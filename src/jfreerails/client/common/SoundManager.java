package jfreerails.client.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;


/**
 * This class is responsible for loading and playing sounds.  Samples are read into a byte arrays so that
 * they don't need to be loaded from disk each time they are played.  When a sample is played, the rate at which
 * it is played back is varied randomly so that it does not always sound the same.
 *
 *  @author Luke
 *
 */
public class SoundManager {
    Random r = new Random();
    private static final SoundManager instance = new SoundManager();

    private static class Sample {
        DataLine.Info info;
        AudioFormat format;
        int size;
        byte[] audio;
    }

    private HashMap samples = new HashMap();

    private SoundManager() {
    }

    private void addClip(String s)
        throws IOException, UnsupportedAudioFileException, 
            LineUnavailableException {
        URL url = getClass().getResource(s);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(loadStream(
                    url.openStream()));

        Sample sample = new Sample();

        sample.format = audioInputStream.getFormat();
        sample.size = (int)(sample.format.getFrameSize() * audioInputStream.getFrameLength());
        sample.audio = new byte[sample.size];
        sample.info = new DataLine.Info(Clip.class, sample.format, sample.size);
        audioInputStream.read(sample.audio, 0, sample.size);
        samples.put(s, sample);
    }

    private ByteArrayInputStream loadStream(InputStream inputstream)
        throws IOException {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];

        for (int i = inputstream.read(data); i != -1;
                i = inputstream.read(data)) {
            bytearrayoutputstream.write(data, 0, i);
        }

        inputstream.close();
        bytearrayoutputstream.close();
        data = bytearrayoutputstream.toByteArray();

        return new ByteArrayInputStream(data);
    }

    public void playSound(String s, int loops) {
        try {
            if (!samples.containsKey(s)) {
                addClip(s);
            }

            Sample sample = (Sample)samples.get(s);

            //Add a random component to the sample rate so that the samples 
            //don't always sound the same.
            float frameRate = (1 + (r.nextFloat() - 0.5f) / 3) * sample.format.getFrameRate();

            //Make sure the frameRate is a sensible value, i.e.  between 4 kHz and 48 kHz.
            frameRate = Math.min(frameRate, 48000f);
            frameRate = Math.max(frameRate, 4000f);

            AudioFormat format = new AudioFormat(sample.format.getEncoding(),
                    sample.format.getSampleRate(),
                    sample.format.getSampleSizeInBits(),
                    sample.format.getChannels(), sample.format.getFrameSize(),
                    frameRate, sample.format.isBigEndian());
            Clip clip = (Clip)AudioSystem.getLine(sample.info);
            clip.open(format, sample.audio, 0, sample.size);
            clip.loop(loops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            SoundManager soundPlayer = getSoundManager();
            soundPlayer.playSound("/jfreerails/client/sounds/cash.wav", 10);
            Thread.sleep(40);
            soundPlayer.playSound("/jfreerails/client/sounds/cash.wav", 10);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SoundManager getSoundManager() {
        return instance;
    }
}