package jfreerails.server;

import java.net.URL;

import jfreerails.server.common.TileSetFactory;
import jfreerails.server.parser.Track_TilesHandlerImpl;
import jfreerails.world.accounts.BondTransaction;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameSpeed;
import jfreerails.world.common.GameTime;
import jfreerails.world.player.Player;
import jfreerails.world.top.GameRules;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.WagonAndEngineTypesFactory;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;

/**
 * Stores a static world object and provides copies to clients.
 * 
 * @author Luke Lindsay
 * 
 */
public class MapFixtureFactory2 {

	private static World w;

	/**
	 * Returns a world object with a map of size 25*25, 4 players, and track,
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
		World world = new WorldImpl(25, 25);
		TileSetFactory tileFactory = new NewTileSetFactoryImpl();

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
			Player p = new Player(name, null, i); // public key set to null!
			int index = world.addPlayer(p);
			assert (index == i);
			world
					.addTransaction(BondTransaction.issueBond(5), p
							.getPrincipal());
			world
					.addTransaction(BondTransaction.issueBond(5), p
							.getPrincipal());
		}
		world.set(ITEM.CALENDAR, new GameCalendar(1200, 1840));
		world.set(ITEM.TIME, new GameTime(0));
		world.set(ITEM.GAME_SPEED, new GameSpeed(10));
		world.set(ITEM.GAME_RULES, GameRules.DEFAULT_RULES);

		return world;
	}

}
