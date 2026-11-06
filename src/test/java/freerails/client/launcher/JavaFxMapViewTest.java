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

package freerails.client.launcher;

import freerails.util.Vector2D;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Tests coordinate translation used by the JavaFX map input boundary. */
public class JavaFxMapViewTest {

    /** Checks that pointer coordinates include the current scrolled map origin. */
    @Test
    public void translatesViewportCoordinatesToMapTiles() {
        assertEquals(new Vector2D(2, 4),
                JavaFxMapView.mapPointToTile(96, 144, 22, 72, 48));
    }

    /** Checks that invalid tile scales fail clearly. */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidTileScale() {
        JavaFxMapView.mapPointToTile(0, 0, 10, 10, 0);
    }
}
