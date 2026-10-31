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

/*
 *
 */
package freerails.model.track.pathfinding;

import freerails.io.GsonManager;
import freerails.model.economy.Money;
import freerails.model.player.Player;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainCategory;
import freerails.model.terrain.TileTransition;
import freerails.model.track.BuildTrackStrategy;
import freerails.model.track.TrackType;
import freerails.util.Utils;
import freerails.util.Vector2D;
import freerails.model.world.World;
import freerails.model.game.Rules;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Test for TrackPathFinder.
 */
public class TrackPathFinderTest extends TestCase {

    private final Player testPlayer = new Player(0, "test");
    private World world;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // load terrain types
        SortedSet<Terrain> terrainTypes = new TreeSet<>();
        terrainTypes.add(new Terrain(0, "Terrain", TerrainCategory.COUNTRY, Money.ZERO, Money.ZERO, Utils.immutableList(new ArrayList<>()), Utils.immutableList(new ArrayList<>()), Utils.immutableList(new ArrayList<>())));

        // generate track types
        SortedSet<TrackType> trackTypes = WorldGenerator.testTrackTypes();

        // load rules
        URL url = TrackPathFinderTest.class.getResource("/rules.without_restrictions.json");
        File file = new File(url.toURI());
        Rules rules = GsonManager.load(file, Rules.class);

        world = new World.Builder().setMapSize(new Vector2D(20, 20)).setTerrainTypes(terrainTypes).setTrackTypes(trackTypes).setRules(rules).build();
        world.addPlayer(testPlayer);
    }

    /**
     * Tests generate path behavior.
     */
    public void testGeneratePath() {
        try {
            BuildTrackStrategy buildTrackStrategy = BuildTrackStrategy.getSingleRuleInstance(0, world);

            TrackPathFinder pathFinder = new TrackPathFinder(world, testPlayer);
            List l = pathFinder.generatePath(Vector2D.ZERO, new Vector2D(0,5), buildTrackStrategy);
            assertEquals(5, l.size());

            List list2 = pathFinder.generatePath(new Vector2D(5, 5),new Vector2D(5, 10), buildTrackStrategy);
            assertEquals(5, list2.size());

            list2 = pathFinder.generatePath(new Vector2D(5, 10), new Vector2D(5,5), buildTrackStrategy);
            assertEquals(5, list2.size());
        } catch (PathNotFoundException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Tests repeated queries return the same route and tie break behavior.
     */
    public void testRepeatedQueriesReturnTheSameRouteAndTieBreak() {
        BuildTrackStrategy strategy = BuildTrackStrategy.getSingleRuleInstance(0, world);
        TrackPathFinder pathFinder = new TrackPathFinder(world, testPlayer);
        try {
            List<Vector2D> first = pathFinder.generatePath(new Vector2D(2, 2), new Vector2D(4, 3), strategy);
            List<Vector2D> second = pathFinder.generatePath(new Vector2D(2, 2), new Vector2D(4, 3), strategy);
            List<Vector2D> third = pathFinder.generatePath(new Vector2D(2, 2), new Vector2D(4, 3), strategy);

            assertEquals(first, second);
            assertEquals(second, third);
            assertEquals(List.of(new Vector2D(3, 3), new Vector2D(4, 3)), first);

            TileTransition[] expectedTransitions = {TileTransition.SOUTH_EAST, TileTransition.EAST};
            assertEquals(expectedTransitions.length, first.size());
            for (int i = 0; i < expectedTransitions.length; i++) {
                Vector2D from = i == 0 ? new Vector2D(2, 2) : first.get(i - 1);
                Vector2D to = first.get(i);
                assertEquals(expectedTransitions[i], TileTransition.getInstance(Vector2D.subtract(to, from)));
            }
        } catch (PathNotFoundException e) {
            fail("Expected the open-map route to be found: " + e.getMessage());
        }
    }
}