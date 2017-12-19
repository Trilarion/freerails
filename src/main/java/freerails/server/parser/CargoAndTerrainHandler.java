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
 * Defines methods to handle parsing the cargo and terrain types XML.
 *
 */
public interface CargoAndTerrainHandler {
    /**
     * An empty element event handling method.
     *
     * @param meta
     * @throws org.xml.sax.SAXException
     */
    void handle_Converts(final Attributes meta) throws SAXException;

    /**
     * A container element start event handling method.
     *
     * @param meta attributes
     * @throws org.xml.sax.SAXException
     */
    void start_Tile(final Attributes meta) throws SAXException;

    /**
     * A container element end event handling method.
     *
     * @throws org.xml.sax.SAXException
     */
    void end_Tile() throws SAXException;

    /**
     * An empty element event handling method.
     *
     * @param meta
     * @throws org.xml.sax.SAXException
     */
    void handle_Cargo(final Attributes meta) throws SAXException;

    /**
     * A container element start event handling method.
     *
     * @param meta attributes
     * @throws org.xml.sax.SAXException
     */
    void start_Cargo_Types(final Attributes meta) throws SAXException;

    /**
     * A container element end event handling method.
     *
     * @throws org.xml.sax.SAXException
     */
    void end_Cargo_Types() throws SAXException;

    /**
     * A container element start event handling method.
     *
     * @param meta attributes
     * @throws org.xml.sax.SAXException
     */
    void start_Terrain_Types(final Attributes meta) throws SAXException;

    /**
     * A container element end event handling method.
     *
     * @throws org.xml.sax.SAXException
     */
    void end_Terrain_Types() throws SAXException;

    /**
     * A container element start event handling method.
     *
     * @param meta attributes
     * @throws org.xml.sax.SAXException
     */
    void start_Types(final Attributes meta) throws SAXException;

    /**
     * A container element end event handling method.
     *
     * @throws org.xml.sax.SAXException
     */
    void end_Types() throws SAXException;

    /**
     * An empty element event handling method.
     *
     * @param meta
     * @throws org.xml.sax.SAXException
     */
    void handle_Consumes(final Attributes meta) throws SAXException;

    /**
     * An empty element event handling method.
     *
     * @param meta
     * @throws org.xml.sax.SAXException
     */
    void handle_Produces(final Attributes meta) throws SAXException;
}