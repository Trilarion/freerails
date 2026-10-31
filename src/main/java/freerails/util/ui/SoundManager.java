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

package freerails.util.ui;

import org.apache.log4j.Logger;

import javax.sound.sampled.*;
import java.io.IOException;
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
public class SoundManager implements LineListener {

    private static final Logger logger = Logger.getLogger(SoundManager.class.getName());
    private static final SoundManager INSTANCE = new SoundManager();
    private final Map<String, AudioSample> samples = new HashMap<>();
    private final Deque<Clip> voices = new LinkedList<>();
    private int maxLines;
    private Mixer mixer;
    private volatile boolean playingSounds = true;

    /**
     * Selects an audio mixer capable of playing the manager's sound format.
     */
    private SoundManager() {
        AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, false);
        DataLine.Info lineInfo = new DataLine.Info(Clip.class, format);
        for (Mixer.Info mo : AudioSystem.getMixerInfo()) {
            Mixer candidate = AudioSystem.getMixer(mo);
            int candidateMaxLines = candidate.getMaxLines(lineInfo);
            if (candidateMaxLines > 0) {
                mixer = candidate;
                maxLines = candidateMaxLines;
                if (maxLines >= 32) {
                    break;
                }
            }
        }
    }

    /**
     * Returns the shared sound manager instance.
     *
     * @return singleton sound manager
     */
    public static SoundManager getInstance() {
        return INSTANCE;
    }

    /**
     * Loads and caches a sound resource for later playback.
     *
     * @param s classpath resource path of the sound
     * @throws IOException if the resource is missing, empty, or cannot be read
     * @throws UnsupportedAudioFileException if the resource format is unsupported
     */
    public synchronized void addClip(String s) throws IOException, UnsupportedAudioFileException {
        if (samples.containsKey(s)) {
            return;
        }

        URL url = getClass().getResource(s);
        if (url == null) {
            throw new IOException("Sound resource not found: " + s);
        }
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url)) {
            AudioFormat format = audioInputStream.getFormat();
            byte[] audio = audioInputStream.readAllBytes();
            if (audio.length == 0) {
                throw new IOException("Sound resource is empty: " + s);
            }
            samples.put(s, new AudioSample(format, audio,
                    new DataLine.Info(Clip.class, format, audio.length)));
        }
    }

    /**
     * Plays a sound, reusing an available audio line when necessary.
     *
     * @param s classpath resource path of the sound
     * @param loops number of repetitions as interpreted by {@link Clip#loop(int)}
     */
    public synchronized void playSound(String s, int loops) {
        if (!playingSounds) {
            return;
        }
        if (mixer == null || maxLines == 0) {
            logger.warn("No audio mixer is available; cannot play sound " + s);
            return;
        }
        try {
            addClip(s);
            AudioSample sample = samples.get(s);
            Clip clip;
            if (voices.size() < maxLines) {
                clip = (Clip) mixer.getLine(sample.info());
            } else {
                clip = voices.removeFirst();
                clip.stop();
                clip.flush();
                clip.close();
            }
            try {
                clip.addLineListener(this);
                clip.open(sample.format(), sample.audio(), 0, sample.audio().length);
                voices.addLast(clip);
                clip.loop(loops);
            } catch (LineUnavailableException | IllegalArgumentException e) {
                clip.close();
                throw e;
            }
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException
                 | IllegalArgumentException e) {
            logger.warn("Could not play sound " + s, e);
        }
    }

    /**
     * Releases a clip after it reports that playback has stopped.
     *
     * @param event audio line event
     */
    @Override
    public synchronized void update(LineEvent event) {
        if (event.getType() == LineEvent.Type.STOP && event.getSource() instanceof Clip clip
                && voices.remove(clip)) {
            clip.close();
        }
    }

    /**
     * Indicates whether sound playback is enabled.
     *
     * @return {@code true} if sounds may be played
     */
    public boolean isPlayingSounds() {
        return playingSounds;
    }

    /**
     * Enables or disables sound playback.
     *
     * @param playingSounds {@code true} to enable playback
     */
    public void setPlayingSounds(boolean playingSounds) {
        this.playingSounds = playingSounds;
    }

    /**
     * Stores the audio data and properties of a sample.
     *
     * @param format format used to decode and play the sample
     * @param audio encoded audio bytes
     * @param info audio-line metadata for the sample
     */
    private record AudioSample(AudioFormat format, byte[] audio, DataLine.Info info) {
    }

}