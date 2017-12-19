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
 * TrainCrashException.java
 *
 * Created on January 25, 2005, 2:32 PM
 */

package freerails.move;

/**
 */
public class TrainCrashException extends Exception {
    private static final long serialVersionUID = 3978710596948342065L;

    private int trainA;

    private int trainB;

    /**
     *
     */
    public TrainCrashException() {

    }

    /**
     *
     * @param aTrain
     * @param bTrain
     */
    public TrainCrashException(int aTrain, int bTrain) {
        trainA = aTrain;
        trainB = bTrain;
    }

    /**
     *
     * @return
     */
    public int getTrainA() {
        return trainA;
    }

    /**
     *
     * @return
     */
    public int getTrainB() {
        return trainB;
    }
}
