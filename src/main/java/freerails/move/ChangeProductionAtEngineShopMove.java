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
package freerails.move;


import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.train.TrainTemplate;
import freerails.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * This Move changes what is being built at an engine shop - when a client wants
 * to build a train, it should send an instance of this class to the server.
 */
public class ChangeProductionAtEngineShopMove implements Move {

    private static final long serialVersionUID = 3905519384997737520L;
    private final List<TrainTemplate> production;
    private final int stationId;
    private final Player player;

    /**
     * @param production
     * @param stationId
     * @param player
     */
    public ChangeProductionAtEngineShopMove(@NotNull List<TrainTemplate> production, int stationId, @NotNull Player player) {
        this.production = production;
        this.stationId = stationId;
        this.player = player;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChangeProductionAtEngineShopMove)) return false;

        final ChangeProductionAtEngineShopMove changeProductionAtEngineShopMove = (ChangeProductionAtEngineShopMove) obj;

        if (stationId != changeProductionAtEngineShopMove.stationId) return false;
        if (!Objects.equals(production, changeProductionAtEngineShopMove.production))
            return false;
        return player.equals(changeProductionAtEngineShopMove.player);
    }

    @Override
    public int hashCode() {
        int result;
        result = production != null ? production.hashCode() : 0;
        result = 29 * result + stationId;
        result = 29 * result + player.hashCode();
        return result;
    }

    @NotNull
    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        // Check that the specified station exists.
        if (!Utils.containsId(stationId, world.getStations(player))) {
            return Status.fail("Station not existing.");
        }
        return Status.OK;
    }

    @Override
    public void apply(@NotNull World world) {
        Status status = applicable(world);
        if (!status.isSuccess()) {
            throw new RuntimeException(status.getMessage());
        }

        Station station = world.getStation(player, stationId);
        station.setProduction(production);
    }
}