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

import freerails.model.cargo.CargoBatch;
import freerails.model.finance.Money;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackPiece;
import freerails.model.train.TrainTemplate;
import freerails.model.train.schedule.TrainOrder;
import freerails.util.Vec2D;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

public class ValueObjectEqualityContractTest extends TestCase {

    public void testMoneyEqualityContractAndAmountField() {
        assertEqualContract(new Money(12), new Money(12), new Money(12));
        assertDifferent(new Money(12), new Money(13));
    }

    public void testCargoBatchEqualityContractAndEveryIdentityField() {
        CargoBatch batch = new CargoBatch(1, new Vec2D(2, 3), 4, 5);
        assertEqualContract(batch, new CargoBatch(1, new Vec2D(2, 3), 4, 5),
                new CargoBatch(1, new Vec2D(2, 3), 4, 5));
        assertDifferent(batch, new CargoBatch(2, new Vec2D(2, 3), 4, 5));
        assertDifferent(batch, new CargoBatch(1, new Vec2D(3, 3), 4, 5));
        assertDifferent(batch, new CargoBatch(1, new Vec2D(2, 3), 6, 5));
        assertDifferent(batch, new CargoBatch(1, new Vec2D(2, 3), 4, 6));

        TreeSet<CargoBatch> sortedBatches = new TreeSet<>();
        sortedBatches.add(batch);
        sortedBatches.add(new CargoBatch(1, new Vec2D(2, 3), 4, 5));
        assertEquals(1, sortedBatches.size());
    }

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

    public void testTrainOrderEqualityContractAndEveryOption() {
        TrainOrder order = new TrainOrder(1, List.of(2, 3), true, false);
        assertEqualContract(order, new TrainOrder(1, List.of(2, 3), true, false),
                new TrainOrder(1, List.of(2, 3), true, false));
        assertDifferent(order, new TrainOrder(2, List.of(2, 3), true, false));
        assertDifferent(order, new TrainOrder(1, List.of(3, 2), true, false));
        assertDifferent(order, new TrainOrder(1, List.of(2, 3), false, false));
        assertDifferent(order, new TrainOrder(1, List.of(2, 3), true, true));
    }

    public void testTrainTemplateEqualityContractAndEveryField() {
        TrainTemplate template = new TrainTemplate(1, List.of(2, 3));
        assertEqualContract(template, new TrainTemplate(1, List.of(2, 3)),
                new TrainTemplate(1, List.of(2, 3)));
        assertDifferent(template, new TrainTemplate(2, List.of(2, 3)));
        assertDifferent(template, new TrainTemplate(1, List.of(3, 2)));
        assertDifferent(template, new TrainTemplate(1, List.of(2)));
    }

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

    private void assertDifferent(Object first, Object second) {
        assertFalse(first.equals(second));
        assertFalse(second.equals(first));
    }
}
