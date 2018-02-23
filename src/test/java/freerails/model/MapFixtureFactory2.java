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

package freerails.model;

import freerails.model.world.WorldItem;
import freerails.model.world.SharedKey;
import freerails.move.AddPlayerMove;
import freerails.move.MoveStatus;
import freerails.savegames.MapCreator;
import freerails.savegames.TrackTilesXmlHandlerImpl;
import freerails.util.Vector2D;
import freerails.model.game.GameCalendar;
import freerails.model.game.GameRules;
import freerails.model.game.GameSpeed;
import freerails.model.game.GameTime;
import freerails.model.player.Player;
import freerails.model.terrain.FullTerrainTile;
import freerails.model.terrain.TerrainType;
import freerails.model.train.WagonAndEngineTypesFactory;
import freerails.model.world.FullWorld;
import freerails.model.world.World;

import java.net.URL;

/**
 * Stores a static world object and provides copies to clients.
 */
public class MapFixtureFactory2 {

    private static World world;

    private MapFixtureFactory2() {}

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

    /**
     *
     * @return
     */
    private static World generateWorld() {

        World world = new FullWorld(new Vector2D(50, 50));
        WagonAndEngineTypesFactory.addTypesToWorld(world);
        MapCreator.addTerrainTileTypesList(world);
        URL track_xml_url = MapFixtureFactory2.class.getResource("/freerails/data/track_tiles.xml");
        TrackTilesXmlHandlerImpl trackSetFactory = new TrackTilesXmlHandlerImpl(track_xml_url);

        trackSetFactory.addTrackRules(world);

        // Add 4 players
        for (int i = 0; i < 4; i++) {
            String name = "player" + i;
            Player player = new Player(name, i);
            AddPlayerMove move = AddPlayerMove.generateMove(world, player);
            MoveStatus moveStatus = move.doMove(world, Player.AUTHORITATIVE);
            assert (moveStatus.succeeds());
        }
        world.set(WorldItem.Calendar, new GameCalendar(1200, 1840));
        world.setTime(new GameTime(0));
        world.set(WorldItem.GameSpeed, new GameSpeed(10));
        world.set(WorldItem.GameRules, GameRules.DEFAULT_RULES);

        int clearTypeID = 0;
        // Fill the world with clear terrain.
        for (int i = 0; i < world.size(SharedKey.TerrainTypes); i++) {
            TerrainType tt = (TerrainType) world.get(SharedKey.TerrainTypes, i);
            if ("Clear".equals(tt.getTerrainTypeName())) {
                clearTypeID = i;
                break;
            }
        }
        FullTerrainTile tile = FullTerrainTile.getInstance(clearTypeID);
        Vector2D mapSize = world.getMapSize();
        for (int x = 0; x < mapSize.x; x++) {
            for (int y = 0; y < mapSize.y; y++) {
                world.setTile(new Vector2D(x, y), tile);
            }
        }

        return world;
    }

}
