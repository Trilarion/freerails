package jfreerails.client.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

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
public class SoundManager implements ModelRootListener {
    private static final Logger logger = Logger.getLogger(SoundManager.class.getName());
    Random r = new Random();
    private boolean playSounds = true;
    private static final SoundManager instance = new SoundManager();

    /**
     *  Stores the audio data and properties of a sample.      
     *
     */
    private static class Sample {
        DataLine.Info info;
        AudioFormat format;
        int size;
        byte[] audio;
    }

    private HashMap<String, Sample> samples = new HashMap<String, Sample>();

    private SoundManager() {
    }

    public void addClip(String s)
        throws IOException, UnsupportedAudioFileException, 
            LineUnavailableException {
    	if (samples.containsKey(s)) {
            return;
        }
    	
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
        if (playSounds) {
            try {
                if (!samples.containsKey(s)) {
                    addClip(s);
                }

                Sample sample = samples.get(s);
                
// The code below is commented out as a fix for bug 1103632	(Sound on Linux).
//                //Add a random component to the sample rate so that the samples 
//                //don't always sound the same.
//                float frameRate = (1 + (r.nextFloat() - 0.5f) / 3) * sample.format.getFrameRate();
//
//                //Make sure the frameRate is a sensible value, i.e.  between 4 kHz and 48 kHz.
//                frameRate = Math.min(frameRate, 48000f);
//                frameRate = Math.max(frameRate, 4000f);
//
//                AudioFormat format = new AudioFormat(sample.format.getEncoding(),
//                        sample.format.getSampleRate(),
//                        sample.format.getSampleSizeInBits(),
//                        sample.format.getChannels(),
//                        sample.format.getFrameSize(), frameRate,
//                        sample.format.isBigEndian());
                Clip clip = (Clip)AudioSystem.getLine(sample.info);
                clip.open(sample.format, sample.audio, 0, sample.size);
                clip.loop(loops);
            } catch (LineUnavailableException e) {
                logger.warning(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SoundManager soundPlayer = getSoundManager();
        soundPlayer.playSound("/jfreerails/client/sounds/cash.wav", 10);

        try {
            Thread.sleep(40);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        soundPlayer.playSound("/jfreerails/client/sounds/cash.wav", 10);
    }

    public static SoundManager getSoundManager() {
        return instance;
    }  

	public void propertyChange(ModelRoot.Property p, Object before, Object after) {
		if(p.equals(ModelRoot.Property.PLAY_SOUNDS)){
			Boolean b = (Boolean)after;
            playSounds = b.booleanValue();
		}
	}
}