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

package freerails.server.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Defines methods to handle parsing the track types XML.
 *
 */
public interface Track_TilesHandler {
    /**
     * A container element start event handling method.
     *
     * @param meta attributes
     * @throws org.xml.sax.SAXException
     */
    void start_CanOnlyBuildOnTheseTerrainTypes(final Attributes meta)
            throws SAXException;

    /**
     * A container element end event handling method.
     *
     * @throws org.xml.sax.SAXException
     */
    void end_CanOnlyBuildOnTheseTerrainTypes() throws SAXException;

    /**
     * A container element start event handling method.
     *
     * @param meta attributes
     * @throws org.xml.sax.SAXException
     */
    void start_ListOfTrackPieceTemplates(final Attributes meta)
            throws SAXException;

    /**
     * A container element end event handling method.
     *
     * @throws org.xml.sax.SAXException
     */
    void end_ListOfTrackPieceTemplates() throws SAXException;

    /**
     * A container element start event handling method.
     *
     * @param meta attributes
     * @throws org.xml.sax.SAXException
     */
    void start_ListOfLegalRoutesAcrossNode(final Attributes meta)
            throws SAXException;

    /**
     * A container element end event handling method.
     *
     * @throws org.xml.sax.SAXException
     */
    void end_ListOfLegalRoutesAcrossNode() throws SAXException;

    /**
     * An empty element event handling method.
     *
     * @param meta
     * @throws org.xml.sax.SAXException
     */
    void handle_LegalRouteAcrossNode(final Attributes meta) throws SAXException;

    /**
     * A container element start event handling method.
     *
     * @param meta attributes
     * @throws org.xml.sax.SAXException
     */
    void start_CannotBuildOnTheseTerrainTypes(final Attributes meta)
            throws SAXException;

    /**
     * A container element end event handling method.
     *
     * @throws org.xml.sax.SAXException
     */
    void end_CannotBuildOnTheseTerrainTypes() throws SAXException;

    /**
     * A container element start event handling method.
     *
     * @param meta attributes
     * @throws org.xml.sax.SAXException
     */
    void start_TrackType(final Attributes meta) throws SAXException;

    /**
     * A container element end event handling method.
     *
     * @throws org.xml.sax.SAXException
     */
    void end_TrackType() throws SAXException;

    /**
     * An empty element event handling method.
     *
     * @param meta
     * @throws org.xml.sax.SAXException
     */
    void handle_TerrainType(final Attributes meta) throws SAXException;

    /**
     * A container element start event handling method.
     *
     * @param meta attributes
     * @throws org.xml.sax.SAXException
     */
    void start_Tiles(final Attributes meta) throws SAXException;

    /**
     * A container element end event handling method.
     * @throws org.xml.sax.SAXException
     */
    void end_Tiles() throws SAXException;

    /**
     * A container element start event handling method.
     *
     * @param meta attributes
     * @throws org.xml.sax.SAXException
     */
    void start_TrackPieceTemplate(final Attributes meta) throws SAXException;

    /**
     * A container element end event handling method.
     *
     * @throws org.xml.sax.SAXException
     */
    void end_TrackPieceTemplate() throws SAXException;

    /**
     * A container element start event handling method.
     *
     * @param meta attributes
     * @throws org.xml.sax.SAXException
     */
    void start_TrackSet(final Attributes meta) throws SAXException;

    /**
     * A container element end event handling method.
     *
     * @throws org.xml.sax.SAXException
     */
    void end_TrackSet() throws SAXException;
}