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

/**
 * Provides serializable actions that encapsulate changes to the game world.
 * Implementations follow the contract documented by {@link Move}; composite,
 * generated, and map-update moves are organized in this package's subpackages.
 * <p>
 * Unless a move documents otherwise, it may be combined with moves that affect
 * independent state. In a {@link CompostMove}, every pair must be orthogonal:
 * both moves are applicable to the same original world, and applying them in
 * either order produces equivalent game state. This is a semantic contract for
 * callers and move authors; it cannot be inferred from move classes alone.
 * <p>
 * In particular, avoid combining track changes on the same or neighboring
 * tiles, a terrain change and a track change on the same tile, or updates to
 * the same field of the same train. Such moves overlap or depend on state
 * affected by the other move. Use an ordered {@link CompositeMove} for
 * dependent moves, or implement a single move for one coherent game action.
 * The combination of an {@link AddTrainMove} and an unconstrained
 * {@link AddTransactionMove} is an example of moves that affect independent
 * state. The track change, station removal, and schedule updates in
 * {@link freerails.move.mapupdatemove.RemoveStationCompositeMove} form one
 * coordinated station-removal action; that grouping should not be taken as
 * evidence that those moves are generally interchangeable or independently
 * reusable.
 *
 * @see <a href="https://github.com/Trilarion/freerails/blob/master/docs/development.md">Package responsibilities and contracts</a>
 */
package freerails.move;