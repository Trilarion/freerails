package jfreerails.server;

import java.net.URL;

import jfreerails.move.AddPlayerMove;
import jfreerails.move.MoveStatus;
import jfreerails.server.common.TileSetFactory;
import jfreerails.server.parser.Track_TilesHandlerImpl;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameSpeed;
import jfreerails.world.common.GameTime;
import jfreerails.world.player.Player;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.GameRules;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.WagonAndEngineTypesFactory;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.track.FreerailsTile;

/**
 * Stores a static world object and provides copies to clients.
 * 
 * @author Luke Lindsay
 * 
 */
public class MapFixtureFactory2 {

    private static World w;

    /**
     * Returns a world object with a map of size 50*50, 4 players, and track,
     * terrain and cargo types as specifed in the xml files used by the actual
     * game.
     */
    synchronized public static World getCopy() {
        if (null == w) {
            w = generateWorld();
        }
        return w.defensiveCopy();
    }

    private static World generateWorld() {
        World world = new WorldImpl(50, 50);
        TileSetFactory tileFactory = new TileSetFactoryImpl();

        WagonAndEngineTypesFactory wetf = new WagonAndEngineTypesFactory();

        wetf.addTypesToWorld(world);

        tileFactory.addTerrainTileTypesList(world);

        URL track_xml_url = OldWorldImpl.class
                .getResource("/jfreerails/data/track_tiles.xml");

        Track_TilesHandlerImpl trackSetFactory = new Track_TilesHandlerImpl(
                track_xml_url);

        trackSetFactory.addTrackRules(world);

        // Add 4 players
        for (int i = 0; i < 4; i++) {
            String name = "player" + i;
            Player p = new Player(name, i);
            AddPlayerMove move = AddPlayerMove.generateMove(world, p);
            MoveStatus ms = move.doMove(world, Player.AUTHORITATIVE);
            assert (ms.ok);
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
        FreerailsTile tile = FreerailsTile.getInstance(clearTypeID);
        for (int x = 0; x < world.getMapWidth(); x++) {
            for (int y = 0; y < world.getMapHeight(); y++) {
                world.setTile(x, y, tile);
            }
        }

        return world;
    }

}
