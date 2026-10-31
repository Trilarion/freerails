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

package freerails.model.world;

import freerails.model.terrain.TileTransition;
import freerails.model.train.PathOnTiles;
import freerails.model.train.TrainState;
import freerails.model.ModelConstants;
import freerails.model.train.motion.TrainMotion;
import freerails.util.Vec2D;

/** Factory for simple train motions in tests. */
public final class TestTrainMotion {

    private TestTrainMotion() {
    }

    public static TrainMotion of(double duration) {
        return of(Double.MIN_VALUE, duration);
    }

    public static TrainMotion of(double startTime, double duration) {
        PathOnTiles path = new PathOnTiles(new Vec2D((int) duration, 0), TileTransition.EAST);
        TrainMotion motion = new TrainMotion(path, ModelConstants.WAGON_LENGTH, duration, TrainState.READY);
        return motion.withStartTime(startTime);
    }
}
