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

import freerails.io.GsonManager;
import freerails.model.cargo.Cargo;
import freerails.model.terrain.Terrain;
import freerails.model.train.Engine;
import freerails.model.world.WorldItem;
import freerails.move.AddPlayerMove;
import freerails.move.MoveStatus;
import freerails.savegames.MapCreator;
import freerails.savegames.TrackTilesXmlHandlerImpl;
import freerails.util.Utils;
import freerails.util.Vec2D;
import freerails.model.game.GameCalendar;
import freerails.model.game.GameRules;
import freerails.model.game.GameSpeed;
import freerails.model.game.GameTime;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import freerails.model.world.World;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.SortedSet;

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
            try {
                world = generateWorld();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        /**
         * Returns a copy of this world object - making changes to this copy will
         * not change this object.
         */
        return (World) Utils.cloneBySerialisation(world);
    }

    /**
     *
     * @return
     */
    private static World generateWorld() throws IOException, URISyntaxException {

        URL url = MapCreator.class.getResource("/freerails/data/scenario/engines.json");
        File file = null;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        SortedSet<Engine> engines;
        try {
            engines = GsonManager.loadEngines(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // load cargo types
        url = MapCreator.class.getResource("/freerails/data/scenario/cargo_types.json");
        file = new File(url.toURI());
        SortedSet<Cargo> cargos = GsonManager.loadCargoTypes(file);

        // load terrain types
        url = MapCreator.class.getResource("/freerails/data/scenario/terrain_types.json");
        file = new File(url.toURI());
        SortedSet<Terrain> terrainTypes = GsonManager.loadTerrainTypes(file);

        World world = new World.Builder().setEngines(engines).setMapSize(new Vec2D(50, 50)).setCargos(cargos).setTerrainTypes(terrainTypes).build();
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
        for (Terrain terrain: world.getTerrains()) {
            if (terrain.getName().equals("Clear")) {
                clearTypeID = terrain.getId();
            }
        }
        TerrainTile tile = new TerrainTile(clearTypeID);
        Vec2D mapSize = world.getMapSize();
        for (int x = 0; x < mapSize.x; x++) {
            for (int y = 0; y < mapSize.y; y++) {
                world.setTile(new Vec2D(x, y), tile);
            }
        }

        return world;
    }

}
