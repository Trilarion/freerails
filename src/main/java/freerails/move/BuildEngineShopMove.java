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

package freerails.move;

import freerails.model.building.EngineShop;
import freerails.model.finance.Money;
import freerails.model.finance.transaction.Transaction;
import freerails.model.finance.transaction.TransactionCategory;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.Objects;

/**
 * Builds an engine shop at an existing station.
 */
public class BuildEngineShopMove implements Move {

    @Serial
    private static final long serialVersionUID = 1L;
    /** Construction cost charged to the owner. */
    public static final Money BUILD_COST = new Money(10_000);

    private final Player player;
    private final EngineShop engineShop;

    /**
     * Creates a move to build the specified engine shop.
     *
     * @param player owner of the shop
     * @param engineShop shop to build
     */
    public BuildEngineShopMove(@NotNull Player player, @NotNull EngineShop engineShop) {
        this.player = player;
        this.engineShop = new EngineShop(engineShop);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BuildEngineShopMove other)) {
            return false;
        }
        return Objects.equals(player, other.player)
                && engineShop.getId() == other.engineShop.getId()
                && Objects.equals(engineShop.getLocation(), other.engineShop.getLocation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, engineShop.getId(), engineShop.getLocation());
    }

    @NotNull
    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        boolean atStation = world.getStations(player).stream()
                .map(Station::getLocation)
                .anyMatch(engineShop.getLocation()::equals);
        if (!atStation) {
            return Status.fail("Engine shops must be built at an existing station.");
        }
        if (world.getEngineShops(player).stream()
                .anyMatch(shop -> shop.getLocation().equals(engineShop.getLocation()))) {
            return Status.fail("An engine shop already exists at this station.");
        }
        if (world.getEngineShops(player).stream().anyMatch(shop -> shop.getId() == engineShop.getId())) {
            return Status.fail("Engine shop with id already existing.");
        }
        if (world.getCurrentBalance(player).amount() < BUILD_COST.amount()) {
            return Status.fail("You can't afford that!");
        }
        return Status.OK;
    }

    @Override
    public void apply(@NotNull World world) {
        Status status = applicable(world);
        if (!status.isSuccess()) {
            throw new IllegalStateException(status.getMessage());
        }
        world.addEngineShop(player, new EngineShop(engineShop));
        Transaction transaction = new Transaction(TransactionCategory.STATIONS,
                new Money(-BUILD_COST.amount()), world.getClock().getCurrentTime());
        world.addTransaction(player, transaction);
    }
}
