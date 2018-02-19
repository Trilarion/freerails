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

package freerails.server;

import freerails.model.station.CalculateCargoSupplyRateAtStation;
import freerails.move.listmove.ChangeStationMove;
import freerails.move.Move;
import freerails.network.movereceiver.MoveReceiver;
import freerails.model.world.PlayerKey;
import freerails.model.NonNullElementWorldIterator;
import freerails.model.world.World;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.station.Station;

/**
 * Loops through all of the known stations and recalculates the
 * cargoes that they supply, demand, and convert.
 */
// TODO relation to CargoAtStationsUpdater?
public class SupplyAtStationsUpdater {

    private final World world;
    private final MoveReceiver moveReceiver;

    /**
     * Constructor, currently called from GUIComponentFactory.
     *
     * @param world The World object that contains all about the game world
     */
    public SupplyAtStationsUpdater(World world, MoveReceiver moveReceiver) {
        this.world = world;
        this.moveReceiver = moveReceiver;
    }

    /**
     * Loop through each known station, call calculations method.
     */
    public void update() {
        for (int i = 0; i < world.getNumberOfPlayers(); i++) {
            FreerailsPrincipal principal = world.getPlayer(i).getPrincipal();
            NonNullElementWorldIterator iterator = new NonNullElementWorldIterator(PlayerKey.Stations, world, principal);

            while (iterator.next()) {
                Station stationBefore = (Station) iterator.getElement();
                CalculateCargoSupplyRateAtStation supplyRate;
                supplyRate = new CalculateCargoSupplyRateAtStation(world, stationBefore.location);

                Station stationAfter = supplyRate.calculations(stationBefore);

                if (!stationAfter.equals(stationBefore)) {
                    Move move = new ChangeStationMove(iterator.getIndex(), stationBefore, stationAfter, principal);
                    moveReceiver.process(move);
                }
            }
        }
    }
}