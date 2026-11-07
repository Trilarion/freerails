/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the FreeRails Team, either version 3 of the License, or
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

import freerails.model.simulation.Time;
import freerails.model.operations.cargo.CargoBatch;
import freerails.model.economy.Money;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackPiece;
import freerails.model.operations.train.TrainTemplate;
import freerails.model.operations.train.schedule.TrainOrder;
import freerails.util.IntPoint2D;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

/**
 * Tests value object equality contract behavior.
 */
public class ValueObjectEqualityContractTest extends TestCase {

    /**
     * Tests money equality contract and amount field behavior.
     */
    public void testMoneyEqualityContractAndAmountField() {
        assertEqualContract(new Money(12), new Money(12), new Money(12));
        assertDifferent(new Money(12), new Money(13));
    }

    /**
     * Tests cargo batch equality contract and every identity field behavior.
     */
    public void testCargoBatchEqualityContractAndEveryIdentityField() {
        CargoBatch batch = new CargoBatch(1, new IntPoint2D(2, 3), new Time(4), 5);
        assertEqualContract(batch, new CargoBatch(1, new IntPoint2D(2, 3), new Time(4), 5),
                new CargoBatch(1, new IntPoint2D(2, 3), new Time(4), 5));
        assertDifferent(batch, new CargoBatch(2, new IntPoint2D(2, 3), new Time(4), 5));
        assertDifferent(batch, new CargoBatch(1, new IntPoint2D(3, 3), new Time(4), 5));
        assertDifferent(batch, new CargoBatch(1, new IntPoint2D(2, 3), new Time(6), 5));
        assertDifferent(batch, new CargoBatch(1, new IntPoint2D(2, 3), new Time(4), 6));

        TreeSet<CargoBatch> sortedBatches = new TreeSet<>();
        sortedBatches.add(batch);
        sortedBatches.add(new CargoBatch(1, new IntPoint2D(2, 3), new Time(4), 5));
        assertEquals(1, sortedBatches.size());
    }

    /**
     * Tests track piece equality contract and every configuration field behavior.
     */
    public void testTrackPieceEqualityContractAndEveryConfigurationField() {
        var type = WorldGenerator.testTrackTypes().first();
        var otherType = WorldGenerator.testTrackTypes().last();
        TrackConfiguration configuration = TrackConfiguration.getFlatInstance(TileTransition.NORTH);
        TrackPiece track = new TrackPiece(configuration, type, 1);
        assertEqualContract(track, new TrackPiece(configuration, type, 1),
                new TrackPiece(configuration, type, 1));
        assertDifferent(track, new TrackPiece(
                TrackConfiguration.getFlatInstance(TileTransition.SOUTH), type, 1));
        assertDifferent(track, new TrackPiece(configuration, otherType, 1));
        assertDifferent(track, new TrackPiece(configuration, type, 2));
    }

    /**
     * Tests terrain tile equality contract and every field behavior.
     */
    public void testTerrainTileEqualityContractAndEveryField() {
        TrackPiece track = new TrackPiece(TrackConfiguration.getFlatInstance(TileTransition.NORTH),
                WorldGenerator.testTrackTypes().first(), 1);
        TerrainTile tile = new TerrainTile(track, 3);
        assertEqualContract(tile, new TerrainTile(track, 3), new TerrainTile(track, 3));
        assertDifferent(tile, new TerrainTile(track, 4));
        assertDifferent(tile, new TerrainTile(
                new TrackPiece(TrackConfiguration.getFlatInstance(TileTransition.SOUTH),
                        track.trackType(), track.playerId()), 3));
        assertDifferent(tile, new TerrainTile(3));
    }

    /**
     * Tests train order equality contract and every option behavior.
     */
    public void testTrainOrderEqualityContractAndEveryOption() {
        TrainOrder order = TrainOrder.specifiedConsist(1, List.of(2, 3), true);
        assertEqualContract(order, TrainOrder.specifiedConsist(1, List.of(2, 3), true),
                TrainOrder.specifiedConsist(1, List.of(2, 3), true));
        assertDifferent(order, TrainOrder.specifiedConsist(2, List.of(2, 3), true));
        assertDifferent(order, TrainOrder.specifiedConsist(1, List.of(3, 2), true));
        assertDifferent(order, TrainOrder.specifiedConsist(1, List.of(2, 3), false));
        assertDifferent(order, TrainOrder.specifiedConsist(1, List.of(2, 3), true).asOneShot());
    }

    /**
     * Tests train template equality contract and every field behavior.
     */
    public void testTrainTemplateEqualityContractAndEveryField() {
        TrainTemplate template = new TrainTemplate(1, List.of(2, 3));
        assertEqualContract(template, new TrainTemplate(1, List.of(2, 3)),
                new TrainTemplate(1, List.of(2, 3)));
        assertDifferent(template, new TrainTemplate(2, List.of(2, 3)));
        assertDifferent(template, new TrainTemplate(1, List.of(3, 2)));
        assertDifferent(template, new TrainTemplate(1, List.of(2)));
    }

    /**
     * Asserts equal contract.
     * @param first the first to use in the test.
     * @param second the second to use in the test.
     * @param third the third to use in the test.
     */
    private void assertEqualContract(Object first, Object second, Object third) {
        assertTrue(first.equals(first));
        assertTrue(first.equals(second));
        assertTrue(second.equals(first));
        assertTrue(second.equals(third));
        assertTrue(third.equals(second));
        assertTrue(first.equals(third));
        assertTrue(third.equals(first));
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(second.hashCode(), third.hashCode());

        HashSet<Object> values = new HashSet<>();
        values.add(first);
        values.add(second);
        values.add(third);
        assertEquals(1, values.size());
    }

    /**
     * Asserts different.
     * @param first the first to use in the test.
     * @param second the second to use in the test.
     */
    private void assertDifferent(Object first, Object second) {
        assertFalse(first.equals(second));
        assertFalse(second.equals(first));
    }
}
