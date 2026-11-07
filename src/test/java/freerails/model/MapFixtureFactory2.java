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

package freerails.model;

import freerails.model.player.Player;
import freerails.model.game.Rules;
import freerails.model.terrain.Terrain;
import freerails.move.AddPlayerMove;
import freerails.move.Status;
import freerails.util.Utils;
import freerails.util.IntPoint2D;
import freerails.model.terrain.TerrainTile;
import freerails.model.world.World;
import freerails.util.WorldGenerator;

/**
 * Stores a static world object and provides copies to clients.
 */
public class MapFixtureFactory2 {

    private static World world;

    /**
     * Creates a map fixture factory2.
     */
    private MapFixtureFactory2() {}

    /**
     * Returns a world object with a map of size 50*50, 4 players, and track,
     * terrain and cargo types as specified in the xml files used by the actual
     * game.
     */
    public static synchronized World getCopy() {
        if (null == world) {
            world = createWorld(null);
        }
        /**
         * Returns a copy of this world object - making changes to this copy will
         * not change this object.
         */
        return (World) Utils.cloneBySerialisation(world);
    }

    /**
     * Returns a fresh fixture world using the supplied rules.
     *
     * @param rules game rules
     * @return a world copy with fixture players and terrain
     */
    public static synchronized World getCopy(Rules rules) {
        return (World) Utils.cloneBySerialisation(createWorld(rules));
    }

    /**
     * Creates the fixture world with four players and a clear terrain map.
     *
     * @param rules rules to use, or {@code null} for the default rules
     * @return the initialized fixture world
     */
    private static World createWorld(Rules rules) {
        World created = rules == null ? WorldGenerator.defaultWorld() : WorldGenerator.defaultWorld(rules);

        for (int i = 0; i < 4; i++) {
            Player player = new Player(i, "player" + i);
            AddPlayerMove move = new AddPlayerMove(player);
            Status status = move.applicable(created);
            assert status.isSuccess();
            move.apply(created);
        }

        int clearTypeID = 0;
        for (Terrain terrain : created.getTerrains()) {
            if (terrain.getName().equals("Clear")) {
                clearTypeID = terrain.getId();
            }
        }
        TerrainTile tile = new TerrainTile(clearTypeID);
        IntPoint2D mapSize = created.getMapSize();
        for (int x = 0; x < mapSize.x(); x++) {
            for (int y = 0; y < mapSize.y(); y++) {
                created.setTile(new IntPoint2D(x, y), tile);
            }
        }
        return created;
    }

}
