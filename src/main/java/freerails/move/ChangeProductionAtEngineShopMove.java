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
package freerails.move;


import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.model.player.Player;
import freerails.model.building.EngineShop;
import freerails.model.operations.train.TrainTemplate;
import freerails.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

/**
 * This Move changes what is being built at an engine shop - when a client wants
 * to build a train, it should send an instance of this class to the server.
 */
public class ChangeProductionAtEngineShopMove implements Move {

    @Serial
    private static final long serialVersionUID = 3905519384997737520L;
    private final List<TrainTemplate> production;
    private final int engineShopId;
    private final Player player;

    /**
     * Creates a move that changes the train production at an engine shop.
     *
     * @param production the train production to configure at the engine shop
     * @param engineShopId the identifier of the engine shop to update
     * @param player the player who owns or performs the operation
     */
    public ChangeProductionAtEngineShopMove(@NotNull List<TrainTemplate> production, int engineShopId, @NotNull Player player) {
        this.production = List.copyOf(production);
        this.engineShopId = engineShopId;
        this.player = player;
    }

    /**
     * Returns whether this object is equal to the specified object.
     *
     * @param obj the object to compare with this object
     * @return true if this object is equal to the specified object, otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChangeProductionAtEngineShopMove changeProductionAtEngineShopMove)) return false;

        if (engineShopId != changeProductionAtEngineShopMove.engineShopId) return false;
        if (!Objects.equals(production, changeProductionAtEngineShopMove.production))
            return false;
        return player.equals(changeProductionAtEngineShopMove.player);
    }

    /**
     * Returns a hash code for this object.
     *
     * @return the hash code for this object
     */
    @Override
    public int hashCode() {
        int result;
        result = production != null ? production.hashCode() : 0;
        result = 29 * result + engineShopId;
        result = 29 * result + player.hashCode();
        return result;
    }

    /**
     * Checks whether this move can be applied to the supplied world.
     *
     * @param world the world whose state is used or updated
     * @return a status indicating whether the move can be applied
     */
    @NotNull
    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        if (!Utils.containsId(engineShopId, world.getEngineShops(player))) {
            return Status.fail("Engine shop not existing.");
        }
        return Status.OK;
    }

    /**
     * Applies this move to the supplied world.
     *
     * @param world the world whose state is used or updated
     */
    @Override
    public void apply(@NotNull World world) {
        Status status = applicable(world);
        if (!status.isSuccess()) {
            throw new RuntimeException(status.getMessage());
        }

        EngineShop engineShop = world.getEngineShop(player, engineShopId);
        engineShop.setProduction(production);
    }
}