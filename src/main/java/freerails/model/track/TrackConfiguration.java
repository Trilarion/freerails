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

package freerails.model.track;

import freerails.model.terrain.TileTransition;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a 9-bit set of track directions and center occupancy on one map
 * tile. Instances are canonical and obtained through the factory methods.
 */
public class TrackConfiguration implements TrackConfigurations {

    private static final long serialVersionUID = 3618695301330974512L;
    private static final List<TrackConfiguration> flatTrackConfigurations;

    static {
        List<TrackConfiguration> configurations = new ArrayList<>(512);

        for (int i = 0; i < 512; i++) {
            configurations.add(i, new TrackConfiguration(i));
        }

        flatTrackConfigurations = configurations;
    }

    private final int length;
    private final int configuration;

    /**
     * Creates a configuration and calculates its total track length.
     *
     * @param configuration 9-bit template represented by this instance
     */
    private TrackConfiguration(int configuration) {
        this.configuration = configuration;

        // Calculate length.
        int tempLength = 0;
        TileTransition[] vectors = TileTransition.getTransitions();
        for (TileTransition vector : vectors) {
            if (contains(vector.get9bitTemplate())) {
                tempLength += vector.getLength();
            }
        }

        length = tempLength;
    }

    /**
     * Combines two configurations by taking the union of their template bits.
     *
     * @param c first configuration
     * @param v second configuration
     * @return canonical configuration containing bits from both inputs
     */
    public static TrackConfiguration add(TrackConfigurations c, TrackConfigurations v) {
        int newTemplate = c.get9bitTemplate() | v.get9bitTemplate();

        return from9bitTemplate(newTemplate);
    }

    /**
     * Returns the canonical configuration represented by a 9-bit template.
     *
     * @param i 9-bit track template in the range 0 through 511
     * @return the canonical configuration for the template
     * @throws IndexOutOfBoundsException if {@code i} is outside the supported range
     */
    public static TrackConfiguration from9bitTemplate(int i) {
        return flatTrackConfigurations.get(i);
    }

    /**
     * Converts a track-configuration value to its canonical instance.
     *
     * @param v configuration whose template is used
     * @return the canonical configuration for {@code v}'s template
     */
    public static TrackConfiguration getFlatInstance(TrackConfigurations v) {
        return from9bitTemplate(v.get9bitTemplate());
    }

    /**
     * Parses a binary string as a track template and returns its canonical
     * configuration. The string is reversed to preserve the legacy bit order.
     *
     * @param trackTemplate binary representation of a 9-bit track template
     * @return the canonical configuration represented by the string
     * @throws NumberFormatException if the string is not a valid binary integer
     * @throws IndexOutOfBoundsException if the parsed value is outside 0 through 511
     */
    public static TrackConfiguration getFlatInstance(String trackTemplate) {
        String templateString = trackTemplate;
        // TODO do not do hack
        // Hack - so that result is as expected by earlier written code.
        StringBuffer strb = new StringBuffer(templateString);
        strb = strb.reverse();
        templateString = strb.toString();

        // End of hack
        int i = Integer.parseInt(templateString, 2);

        return flatTrackConfigurations.get(i);
    }

    /**
     * Removes the bits in one configuration from another.
     *
     * @param c configuration to subtract from
     * @param v configuration bits to remove
     * @return canonical configuration representing {@code c} minus {@code v}
     */
    public static TrackConfiguration subtract(TrackConfigurations c, TrackConfigurations v) {
        int newTemplate = c.get9bitTemplate() & ~v.get9bitTemplate();
        return from9bitTemplate(newTemplate);
    }

    /**
     * Tests whether this configuration includes every direction and bit set in
     * another configuration.
     *
     * @param ftt configuration that may be a subset of this one
     * @return {@code true} if this configuration contains all bits in {@code ftt}
     */
    public boolean contains(TrackConfigurations ftt) {
        int trackTemplate = ftt.get9bitTemplate();
        return contains(trackTemplate);
    }


    /**
     * Tests whether this configuration contains every bit in a template.
     *
     * @param trackTemplate template to test
     * @return {@code true} if all bits in {@code trackTemplate} are set here
     */
    public boolean contains(int trackTemplate) {
        return (trackTemplate | configuration) == configuration;
    }

    // TODO just shave the 9th bit of
    /**
     * Returns the 8-bit encoding of the directional track transitions,
     * excluding the center bit.
     *
     * @return the 8-bit directional template
     */
    public int get8bitTemplate() {
        int newTemplate = 0;
        TileTransition[] vectors = TileTransition.getTransitions();

        for (TileTransition vector : vectors) {
            if (contains(vector)) {
                newTemplate = newTemplate | vector.get8bitTemplate();
            }
        }

        return newTemplate;
    }

    // TODO why is a 9th bit needed anyway?
    /**
     * {@inheritDoc}
     *
     * @return this configuration's 9-bit template
     */
    @Override
    public int get9bitTemplate() {
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
     * Returns the 9-bit configuration value.
     *
     * @return the encoded configuration
     */
    public int getConfiguration() {
        return configuration;
    }

    /**
     * Compares configurations by their encoded template.
     *
     * @param obj object to compare
     * @return {@code true} if {@code obj} is an equal configuration
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        final TrackConfiguration that = (TrackConfiguration) obj;

        return configuration == that.configuration;
    }

    /**
     * Returns a hash code derived from the encoded template.
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
    private Object readResolve() {
        return TrackConfiguration.from9bitTemplate(configuration);
    }

    /**
     * Returns a readable description of the center bit and included directions.
     *
     * @return a textual description of this configuration
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int matches = 0;

        if (contains(TrackConfiguration.getFlatInstance("000010000"))) {
            sb.append("tile center");
        } else {
            sb.append("no tile center");
        }

        for (int i = 0; i < 8; i++) {
            TileTransition v = TileTransition.getInstance(i);

            if (contains(v)) {
                sb.append(',');
                sb.append(v);
                matches++;
            }
        }

        return sb.toString().trim();
    }
}