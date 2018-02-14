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
import org.xml.sax.SAXException;

/**
 * Defines methods to handle parsing the cargo and terrain types XML.
 */
public interface CargoAndTerrainXmlHandler {

    /**
     * An empty element event handling method.
     */
    void handleConversions(final Attributes attributes) throws SAXException;

    /**
     * A container element start event handling method.
     *
     * @param attributes attributes
     */
    void startTile(final Attributes attributes) throws SAXException;

    /**
     * A container element end event handling method.
     */
    void endTile();

    /**
     * An empty element event handling method.
     */
    void handleCargo(final Attributes attributes);

    /**
     * An empty element event handling method.
     */
    void handleConsumptions(final Attributes attributes) throws SAXException;

    /**
     * An empty element event handling method.
     */
    void handleProductions(final Attributes attributes) throws SAXException;
}