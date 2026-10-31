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

package freerails.io.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import freerails.io.ARGBColor;

import java.io.IOException;

/**
 * Serializes ARGB colors as hexadecimal JSON strings.
 */
public class ColorAdapter extends TypeAdapter<ARGBColor> {

    /**
     * Writes a color as a hash-prefixed hexadecimal string.
     *
     * @param out JSON writer
     * @param value color to write
     * @throws IOException if writing to the JSON stream fails
     */
    @Override
    public void write(JsonWriter out, ARGBColor value) throws IOException {
        out.value(ARGBColor.toHexString(value));
    }

    /**
     * Reads a hexadecimal color string from the JSON stream.
     *
     * @param in JSON reader
     * @return the parsed color
     * @throws IOException if reading from the JSON stream fails
     * @throws IllegalArgumentException if the string is not a valid color
     */
    @Override
    public ARGBColor read(JsonReader in) throws IOException {
        return ARGBColor.fromHexString(in.nextString());
    }
}
