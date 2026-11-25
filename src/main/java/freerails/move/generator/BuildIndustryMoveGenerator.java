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

package freerails.move.generator;

import freerails.model.economy.Money;
import freerails.model.economy.transaction.ItemTransaction;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.player.Player;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainTile;
import freerails.model.world.UnmodifiableWorld;
import freerails.move.AddTransactionMove;
import freerails.move.CompostMove;
import freerails.move.Move;
import freerails.move.RefusedMove;
import freerails.move.mapupdatemove.ChangeTileMove;
import freerails.util.IntPoint2D;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.List;
import java.util.Optional;

/**
 * Generates the move that builds an industry on a tile and charges the
 * industry's build cost, which the generating world sets.
 *
 * @param location the tile to build on
 * @param terrainId the terrain type of the industry
 * @param player the building player
 */
public record BuildIndustryMoveGenerator(IntPoint2D location, int terrainId, Player player) implements ClientMoveGenerator {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * @param player the player the generated move acts for
     * @return this generator building for the supplied player
     */
    @Override
    public @NotNull BuildIndustryMoveGenerator withPlayer(@NotNull Player player) {
        return new BuildIndustryMoveGenerator(location, terrainId, player);
    }

    /**
     * @param world the world that sets the build cost
     * @return the tile change with its cash-constrained cost, or a refused move
     */
    @Override
    public Move generate(UnmodifiableWorld world) {
        if (location == null || !world.boundsContain(location)) {
            return new RefusedMove("Select a valid map tile before building an industry.");
        }
        Optional<Terrain> terrain = world.getTerrains().stream()
                .filter(candidate -> candidate.getId() == terrainId)
                .findFirst();
        if (terrain.isEmpty() || terrain.get().getIndustryBuildCost() == null) {
            return new RefusedMove("This industry cannot be built.");
        }
        Move changeTile = new ChangeTileMove(new TerrainTile(terrainId, world.getTile(location).trackPiece()), location);
        ItemTransaction transaction = new ItemTransaction(TransactionCategory.INDUSTRIES,
                Money.opposite(terrain.get().getIndustryBuildCost()), world.getClock().getCurrentTime(), 1, terrainId);
        return new CompostMove(List.of(changeTile, new AddTransactionMove(player, transaction, true)));
    }
}
