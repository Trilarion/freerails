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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.model.track;

import freerails.model.terrain.TileTransition;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an 8-bit set of track directions on one map tile. Bit zero is
 * north, with bits increasing clockwise. Instances are canonical.
 */
public class TrackConfiguration implements TrackConfigurations {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final List<TrackConfiguration> flatTrackConfigurations;

    static {
        List<TrackConfiguration> configurations = new ArrayList<>(256);

        for (int i = 0; i < 256; i++) {
            configurations.add(i, new TrackConfiguration(i));
        }

        flatTrackConfigurations = configurations;
    }

    private final int length;
    private final int configuration;

    /**
     * Creates a configuration and calculates its total track length.
     *
     * @param configuration 8-bit directional mask represented by this instance
     */
    private TrackConfiguration(int configuration) {
        this.configuration = configuration;

        int tempLength = 0;
        for (TileTransition transition : TileTransition.getTransitions()) {
            if (contains(transition)) {
                tempLength = (int) (tempLength + transition.getLength());
            }
        }

        length = tempLength;
    }

    /**
     * Combines two configurations by taking the union of their direction bits.
     *
     * @param c first configuration
     * @param v second configuration
     * @return canonical configuration containing bits from both inputs
     */
    public static TrackConfiguration add(TrackConfigurations c, TrackConfigurations v) {
        int newTemplate = c.get8bitTemplate() | v.get8bitTemplate();
        return from8bitTemplate(newTemplate);
    }

    /**
     * Returns the canonical configuration represented by an 8-bit mask.
     *
     * @param template directional mask in the range 0 through 255
     * @return the canonical configuration for the mask
     * @throws IndexOutOfBoundsException if {@code template} is outside 0 through 255
     */
    public static TrackConfiguration from8bitTemplate(int template) {
        return flatTrackConfigurations.get(template);
    }

    /**
     * Converts a track-configuration value to its canonical instance.
     *
     * @param configuration configuration whose directional mask is used
     * @return the canonical configuration for its mask
     */
    public static TrackConfiguration getFlatInstance(TrackConfigurations configuration) {
        return from8bitTemplate(configuration.get8bitTemplate());
    }

    /**
     * Parses a binary string as a directional mask using standard binary order.
     *
     * @param trackTemplate binary representation of an 8-bit directional mask
     * @return the canonical configuration represented by the string
     * @throws NumberFormatException if the string is not exactly eight binary digits
     */
    public static TrackConfiguration getFlatInstance(String trackTemplate) {
        if (trackTemplate.length() != 8) {
            throw new NumberFormatException("Track template must contain exactly 8 binary digits.");
        }
        for (int i = 0; i < trackTemplate.length(); i++) {
            if (trackTemplate.charAt(i) != '0' && trackTemplate.charAt(i) != '1') {
                throw new NumberFormatException("Track template must contain exactly 8 binary digits.");
            }
        }
        return from8bitTemplate(Integer.parseInt(trackTemplate, 2));
    }

    /**
     * Removes the bits in one configuration from another.
     *
     * @param c configuration to subtract from
     * @param v configuration bits to remove
     * @return canonical configuration representing {@code c} minus {@code v}
     */
    public static TrackConfiguration subtract(TrackConfigurations c, TrackConfigurations v) {
        int newTemplate = c.get8bitTemplate() & ~v.get8bitTemplate();
        return from8bitTemplate(newTemplate);
    }

    /**
     * Tests whether this configuration includes every direction in another.
     *
     * @param configuration configuration that may be a subset of this one
     * @return {@code true} if this configuration contains all its direction bits
     */
    public boolean contains(TrackConfigurations configuration) {
        return contains(configuration.get8bitTemplate());
    }

    /**
     * Tests whether this configuration contains every bit in a directional mask.
     *
     * @param template directional mask to test
     * @return {@code true} if all bits in {@code template} are set here
     */
    public boolean contains(int template) {
        return (template | configuration) == configuration;
    }

    /** {@inheritDoc} */
    @Override
    public int get8bitTemplate() {
        return configuration;
    }

    /**
     * Returns the total length of track represented by this configuration.
     *
     * @return summed length of its included transitions
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the encoded directional mask.
     *
     * @return the 8-bit configuration
     */
    public int getConfiguration() {
        return configuration;
    }

    /**
     * Compares configurations by their encoded directional mask.
     *
     * @param obj object to compare
     * @return {@code true} if {@code obj} is an equal configuration
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TrackConfiguration that = (TrackConfiguration) obj;
        return configuration == that.configuration;
    }

    /**
     * Returns a hash code derived from the encoded directional mask.
     *
     * @return the configuration's hash code
     */
    @Override
    public int hashCode() {
        return configuration;
    }

    /**
     * Replaces the deserialized object with its canonical instance.
     *
     * @return the canonical configuration
     */
    @Serial
    private Object readResolve() {
        return TrackConfiguration.from8bitTemplate(configuration);
    }

    /**
     * Returns a readable description of included directions.
     *
     * @return included directions, or {@code "no connections"} when empty
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            TileTransition transition = TileTransition.getInstance(i);
            if (contains(transition)) {
                if (!sb.isEmpty()) {
                    sb.append(',');
                }
                sb.append(transition);
            }
        }

        return sb.isEmpty() ? "no connections" : sb.toString().trim();
    }
}
