/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
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
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Is responsible for loading and playing sounds. Samples are read
 * into a byte arrays so that they don't need to be loaded from disk each time
 * they are played.
 */
public class SoundManager implements ModelRootListener, LineListener {

    private static final Logger logger = Logger.getLogger(SoundManager.class.getName());
    private static final SoundManager soundManager = new SoundManager();
    private final Map<String, AudioSample> samples = new HashMap<>();
    private final Deque<Clip> voices = new LinkedList<>();
    private int maxLines;
    private Mixer mixer;
    private boolean playSounds = true;

    private SoundManager() {
        AudioFormat format2 = new AudioFormat(8000.0f, 16, 1, true, false);
        DataLine.Info info2 = new DataLine.Info(null, format2, 0);
        for (Mixer.Info mo : AudioSystem.getMixerInfo()) {

            mixer = AudioSystem.getMixer(mo);
            maxLines = mixer.getMaxLines(info2);
            if (maxLines >= 32) break; // Java Sound Audio Engine, version 1.0 satisfies this.
        }
        logger.debug("Sound Mixer: " + mixer.getMixerInfo() + '(' + maxLines + " voices).");
    }

    /**
     * @return
     */
    public static SoundManager getSoundManager() {
        return soundManager;
    }

    private static ByteArrayInputStream loadStream(InputStream inputstream) throws IOException {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];

        for (int i = inputstream.read(data); i != -1; i = inputstream.read(data)) {
            bytearrayoutputstream.write(data, 0, i);
        }

        inputstream.close();
        bytearrayoutputstream.close();
        data = bytearrayoutputstream.toByteArray();

        return new ByteArrayInputStream(data);
    }

    /**
     * @param s
     * @throws IOException
     * @throws UnsupportedAudioFileException
     * @throws LineUnavailableException
     */
    public void addClip(String s) throws IOException, UnsupportedAudioFileException {
        if (samples.containsKey(s)) {
            return;
        }

        URL url = getClass().getResource(s);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(loadStream(url.openStream()));

        AudioSample audioSample = new AudioSample();

        audioSample.format = audioInputStream.getFormat();
        audioSample.size = (int) (audioSample.format.getFrameSize() * audioInputStream.getFrameLength());
        audioSample.audio = new byte[audioSample.size];
        audioSample.info = new DataLine.Info(Clip.class, audioSample.format, audioSample.size);
        audioInputStream.read(audioSample.audio, 0, audioSample.size);
        samples.put(s, audioSample);
    }

    /**
     * @param s
     * @param loops
     */
    public void playSound(String s, int loops) {
        if (playSounds) {
            try {
                if (!samples.containsKey(s)) {
                    addClip(s);
                }

                AudioSample audioSample = samples.get(s);

                Clip clip;
                if (voices.size() < maxLines) {
                    clip = (Clip) mixer.getLine(audioSample.info);
                } else {
                    clip = voices.removeFirst();
                    clip.stop();
                    clip.flush();
                    clip.close();
                }
                clip.addLineListener(this);
                clip.open(audioSample.format, audioSample.audio, 0, audioSample.size);
                clip.loop(loops);
                voices.add(clip);
            } catch (LineUnavailableException e) {
                logger.warn(e.getMessage());
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * @param p
     * @param oldValue
     * @param newValue
     */
    public void propertyChange(ModelRoot.Property p, Object oldValue, Object newValue) {
        if (p == ModelRoot.Property.PLAY_SOUNDS) {
            playSounds = (Boolean) newValue;
        }
    }

    public void update(LineEvent event) {
        // TODO free up resources when we have finished playing a clip.
    }

    /**
     * Stores the audio data and properties of a sample.
     */
    private static class AudioSample {

        private byte[] audio;

        private AudioFormat format;

        private DataLine.Info info;

        private int size;
    }

}