/*
 * Created on Sep 4, 2004
 *
 */
package jfreerails.controller;

import java.awt.Point;
import java.util.List;

import jfreerails.world.player.Player;
import jfreerails.world.top.GameRules;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.WorldImpl;
import junit.framework.TestCase;


/**
 * JUnit test for TrackPathFinder.
 * @author Luke
 *
 */
public class TrackPathFinderTest extends TestCase {
    private WorldImpl world;
    private Player testPlayer = new Player("test",
            (new Player("test")).getPublicKey(), 0);

    protected void setUp() throws Exception {
        world = new WorldImpl(20, 20);
        world.addPlayer(testPlayer);
        world.set(ITEM.GAME_RULES, GameRules.NO_RESTRICTIONS);
        MapFixtureFactory.generateTrackRuleList(world);
    }

    public void testGeneratePath() {
        try {
        	BuildTrackStrategy bts = BuildTrackStrategy.getSingleRuleInstance(0, world);
        	
            TrackPathFinder pathFinder = new TrackPathFinder(world, testPlayer.getPrincipal());
            List l = pathFinder.generatePath(new Point(0, 0), new Point(0, 5), bts);
            assertEquals(5, l.size());

            List list2 = pathFinder.generatePath(new Point(5, 5),
                    new Point(5, 10), bts);
            assertEquals(5, list2.size());

            list2 = pathFinder.generatePath(new Point(5, 10), new Point(5, 5), bts);
            assertEquals(5, list2.size());
        } catch (PathNotFoundException e) {
            fail();
        }
    }
}