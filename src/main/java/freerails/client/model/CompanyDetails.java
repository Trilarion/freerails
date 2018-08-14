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

package freerails.client.model;

import java.awt.*;

/**
 * Stores the company details that are used to draw a line and title on the
 * graph.
 */
public class CompanyDetails {

    /**
     * The company's net worth at the end of each year.
     */
    public final long[] value = new long[100];

    /**
     * The colour for the line on the graph.
     */
    public final Color color;

    /**
     * The company's name.
     */
    public final String name;

    public CompanyDetails(String n, Color c) {

        color = c;
        name = n;
        for (int i = 0; i < 100; i++) {
            value[i] = Integer.MIN_VALUE;
        }
    }
}
