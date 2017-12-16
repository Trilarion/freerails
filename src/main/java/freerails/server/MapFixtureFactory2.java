package freerails.server;

import java.net.URL;

import freerails.move.AddPlayerMove;
import freerails.move.MoveStatus;
import freerails.server.common.TileSetFactory;
import freerails.server.parser.Track_TilesHandlerImpl;
import freerails.world.common.GameCalendar;
import freerails.world.common.GameSpeed;
import freerails.world.common.GameTime;
import freerails.world.player.Player;
import freerails.world.terrain.TerrainType;
import freerails.world.top.GameRules;
import freerails.world.top.ITEM;
import freerails.world.top.SKEY;
import freerails.world.top.WagonAndEngineTypesFactory;
import freerails.world.top.World;
import freerails.world.top.WorldImpl;
import freerails.world.track.FreerailsTile;

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
                .getResource("/freerails/data/track_tiles.xml");

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
