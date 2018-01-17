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

import freerails.move.AddPlayerMove;
import freerails.move.MoveStatus;
import freerails.server.parser.Track_TilesHandlerImpl;
import freerails.world.ITEM;
import freerails.world.SKEY;
import freerails.world.World;
import freerails.world.WorldImpl;
import freerails.world.game.GameCalendar;
import freerails.world.game.GameRules;
import freerails.world.game.GameSpeed;
import freerails.world.game.GameTime;
import freerails.world.player.Player;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.terrain.TerrainType;
import freerails.world.train.WagonAndEngineTypesFactory;

import java.net.URL;

/**
 * Stores a static world object and provides copies to clients.
 */
// TODO what is this good for?
public class MapFixtureFactory2 {

    private static World world;

    private MapFixtureFactory2() {
    }

    /**
     * Returns a world object with a map of size 50*50, 4 players, and track,
     * terrain and cargo types as specified in the xml files used by the actual
     * game.
     */
    public static synchronized World getCopy() {
        if (null == world) {
            world = generateWorld();
        }
        return world.defensiveCopy();
    }

    private static World generateWorld() {

        World world = new WorldImpl(50, 50);
        TileSetFactory tileFactory = new TileSetFactoryImpl();
        WagonAndEngineTypesFactory.addTypesToWorld(world);
        tileFactory.addTerrainTileTypesList(world);
        URL track_xml_url = OldWorldImpl.class.getResource("/freerails/data/track_tiles.xml");
        Track_TilesHandlerImpl trackSetFactory = new Track_TilesHandlerImpl(track_xml_url);

        trackSetFactory.addTrackRules(world);

        // Add 4 players
        for (int i = 0; i < 4; i++) {
            String name = "player" + i;
            Player p = new Player(name, i);
            AddPlayerMove move = AddPlayerMove.generateMove(world, p);
            MoveStatus ms = move.doMove(world, Player.AUTHORITATIVE);
            assert (ms.status);
        }
        world.set(ITEM.CALENDAR, new GameCalendar(1200, 1840));
        world.setTime(new GameTime(0));
        world.set(ITEM.GAME_SPEED, new GameSpeed(10));
        world.set(ITEM.GAME_RULES, GameRules.DEFAULT_RULES);

        int clearTypeID = 0;
        // Fill the world with clear terrain.
        for (int i = 0; i < world.size(SKEY.TERRAIN_TYPES); i++) {
            TerrainType tt = (TerrainType) world.get(SKEY.TERRAIN_TYPES, i);
            if ("Clear".equals(tt.getTerrainTypeName())) {
                clearTypeID = i;
                break;
            }
        }
        FullTerrainTile tile = FullTerrainTile.getInstance(clearTypeID);
        for (int x = 0; x < world.getMapWidth(); x++) {
            for (int y = 0; y < world.getMapHeight(); y++) {
                world.setTile(x, y, tile);
            }
        }

        return world;
    }

}
