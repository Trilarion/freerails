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

/*
 *
 */
package freerails.model.track.pathfinding;

import freerails.model.finances.Money;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainCategory;
import freerails.model.track.BuildTrackStrategy;
import freerails.model.track.TrackType;
import freerails.util.Utils;
import freerails.util.Vec2D;
import freerails.model.world.World;
import freerails.model.game.GameRules;
import freerails.model.world.WorldItem;
import freerails.model.player.Player;
import freerails.model.MapFixtureFactory;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Test for TrackPathFinder.
 */
public class TrackPathFinderTest extends TestCase {

    private final Player testPlayer = new Player("test", 0);
    private World world;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // load terrain types
        SortedSet<Terrain> terrainTypes = new TreeSet<>();
        terrainTypes.add(new Terrain(0, "Terrain", TerrainCategory.COUNTRY, Money.ZERO, Money.ZERO, Utils.immutableList(new ArrayList<>()), Utils.immutableList(new ArrayList<>()), Utils.immutableList(new ArrayList<>())));

        // generate track types
        SortedSet<TrackType> trackTypes = MapFixtureFactory.generateTrackRuleList();

        world = new World.Builder().setMapSize(new Vec2D(20, 20)).setTerrainTypes(terrainTypes).setTrackTypes(trackTypes).build();
        world.addPlayer(testPlayer);
        world.set(WorldItem.GameRules, GameRules.NO_RESTRICTIONS);
    }

    /**
     *
     */
    public void testGeneratePath() {
        try {
            BuildTrackStrategy buildTrackStrategy = BuildTrackStrategy.getSingleRuleInstance(0, world);

            TrackPathFinder pathFinder = new TrackPathFinder(world, testPlayer.getPrincipal());
            List l = pathFinder.generatePath(Vec2D.ZERO, new Vec2D(0,5), buildTrackStrategy);
            assertEquals(5, l.size());

            List list2 = pathFinder.generatePath(new Vec2D(5, 5),new Vec2D(5, 10), buildTrackStrategy);
            assertEquals(5, list2.size());

            list2 = pathFinder.generatePath(new Vec2D(5, 10), new Vec2D(5,5), buildTrackStrategy);
            assertEquals(5, list2.size());
        } catch (PathNotFoundException e) {
            e.printStackTrace();
            fail();
        }
    }
}