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

package freerails.server;

import freerails.server.parser.Track_TilesHandlerImpl;
import freerails.util.ProgressMonitor;
import freerails.world.*;
import freerails.world.game.GameCalendar;
import freerails.world.game.GameRules;
import freerails.world.game.GameSpeed;
import freerails.world.game.GameTime;
import org.xml.sax.SAXException;

import java.net.URL;

/**
 * Sets up a World object.
 */
// TODO difference to world.WorldImpl?
public class OldWorldImpl {
    /**
     * Note, the map name is converted to lower case and any spaces are replaced
     * with underscores.
     *
     * @param mapName
     * @param pm
     * @return
     */
    public static World createWorldFromMapFile(String mapName,
                                               ProgressMonitor pm) {

        mapName = mapName.toLowerCase();
        mapName = mapName.replace(' ', '_');

        pm.setValue(0);
        pm.nextStep(7);

        int progess = 0;

        TileSetFactory tileFactory = new TileSetFactoryImpl();
        pm.setValue(++progess);

        WorldImpl w = new WorldImpl();
        pm.setValue(++progess);

        WagonAndEngineTypesFactory wetf = new WagonAndEngineTypesFactory();
        pm.setValue(++progess);
        wetf.addTypesToWorld(w);
        pm.setValue(++progess);

        tileFactory.addTerrainTileTypesList(w);
        pm.setValue(++progess);

        URL track_xml_url = OldWorldImpl.class
                .getResource("/freerails/data/track_tiles.xml");

        Track_TilesHandlerImpl trackSetFactory = new Track_TilesHandlerImpl(
                track_xml_url);
        pm.setValue(++progess);

        trackSetFactory.addTrackRules(w);
        pm.setValue(++progess);

        // Load the terrain map
        URL map_url = OldWorldImpl.class.getResource("/freerails/data/"
                + mapName + ".png");
        MapFactory.setupMap(map_url, w, pm);

        // Load the city names
        URL cities_xml_url = OldWorldImpl.class.getResource("/freerails/data/"
                + mapName + "_cities.xml");

        try {
            CityNamesSAXParser.readCityNames(w, cities_xml_url);
        } catch (SAXException ignored) {
        }

        // Randomly position the city tiles
        CityTilePositioner ctp = new CityTilePositioner(w);
        ctp.initCities();

        // Set the time..
        w.set(ITEM.CALENDAR, new GameCalendar(1200, 1840));
        w.setTime(new GameTime(0));
        w.set(ITEM.GAME_SPEED, new GameSpeed(10));
        w.set(ITEM.GAME_RULES, GameRules.DEFAULT_RULES);

        /*
         * Note, money used to get added to player finances here, now it is done
         * when players are added. See AddPlayerMove
         */
        return w;
    }
}