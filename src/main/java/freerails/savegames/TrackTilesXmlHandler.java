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

package freerails.savegames;

import org.xml.sax.Attributes;

/**
 * Defines methods to handle parsing the track types XML.
 */
public interface TrackTilesXmlHandler {
    /**
     * A container element start event handling method.
     *
     * @param attributes attributes
     */
    void startCanOnlyBuildOnTheseTerrainTypes(final Attributes attributes);

    /**
     * A container element end event handling method.
     */
    void endCanOnlyBuildOnTheseTerrainTypes();

    /**
     * A container element start event handling method.
     *
     * @param attributes attributes
     */
    void startListOfTrackPieceTemplates(final Attributes attributes);

    /**
     * A container element end event handling method.
     */
    void endListOfTrackPieceTemplates();

    /**
     * A container element start event handling method.
     *
     * @param attributes attributes
     */
    void startCannotBuildOnTheseTerrainTypes(final Attributes attributes);

    /**
     * A container element end event handling method.
     */
    void endCannotBuildOnTheseTerrainTypes();

    /**
     * A container element start event handling method.
     *
     * @param attributes attributes
     */
    void startTrackType(final Attributes attributes);

    /**
     * A container element end event handling method.
     */
    void endTrackType();

    /**
     * An empty element event handling method.
     */
    void handleTerrainType(final Attributes attributes);

    /**
     * A container element end event handling method.
     */
    void endTiles();

    /**
     * A container element start event handling method.
     *
     * @param attributes attributes
     */
    void startTrackPieceTemplate(final Attributes attributes);

    /**
     * A container element start event handling method.
     *
     * @param attributes attributes
     */
    void startTrackSet(final Attributes attributes);

}