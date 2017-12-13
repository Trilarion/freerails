/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jfreerails.world.train;

import java.util.Arrays;
import jfreerails.world.common.*;


public class TrainModel implements FreerailsSerializable {
    public static final int MAX_NUMBER_OF_WAGONS = 10;
    private int scheduleID;
    TrainPositionOnMap trainposition;
    int engineType = 0;
    final int[] wagonTypes;
    private int cargoBundleNumber;
    private final GameTime creationDate;

    /**
     * copy constructor with original schedule, cargo, position, but new
     * engine and wagons
     */
    public TrainModel getNewInstance(int newEngine, int[] newWagons) {
        return new TrainModel(newEngine, newWagons, this.getPosition(),
            this.getScheduleID(), this.getCargoBundleNumber(), creationDate);
    }

    /**
     * @return the date at which the engine was created
     */
    public GameTime getCreationDate() {
	return creationDate;
    }

    /**
     * Constructor for a new train.
     * @param engine type of the engine
     * @param wagons array of indexes into the WAGON_TYPES table
     * @param p initial position of the train on the map.
     */
    public TrainModel(int engine, int[] wagons, TrainPositionOnMap p, int
	    scheduleID, int bundleId, GameTime creationDate) {
	engineType = engine;
	wagonTypes = wagons;
	trainposition = p;
	this.scheduleID = scheduleID;
	cargoBundleNumber = bundleId;
	this.creationDate = creationDate;
    }

    public int getLength() {
        return (1 + wagonTypes.length) * 32; //Engine + wagons.
    }

    public boolean canAddWagon() {
        return wagonTypes.length < MAX_NUMBER_OF_WAGONS;
    }

    public int getNumberOfWagons() {
        return wagonTypes.length;
    }

    /**
     * @return Index into WAGON_TYPES table of the ith wagon in the train
     */
    public int getWagon(int i) {
        return wagonTypes[i];
    }

    public TrainPositionOnMap getPosition() {
        return trainposition;
    }

    public void setPosition(TrainPositionOnMap s) {
        trainposition = s;
    }

    /**
     * @return an index into the ENGINE_TYPES database
     */
    public int getEngineType() {
        return engineType;
    }

    public int getCargoBundleNumber() {
        return cargoBundleNumber;
    }

    public int getScheduleID() {
        return scheduleID;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TrainModel) {
            TrainModel test = (TrainModel)obj;
            boolean b = this.cargoBundleNumber == test.cargoBundleNumber &&
                this.engineType == test.engineType &&
                null == this.trainposition ? null == test.trainposition
                                           : this.trainposition.equals(test.trainposition) &&
                Arrays.equals(this.wagonTypes, test.wagonTypes) &&
                this.scheduleID == test.scheduleID;

            return b;
        } else {
            return false;
        }
    }
}
