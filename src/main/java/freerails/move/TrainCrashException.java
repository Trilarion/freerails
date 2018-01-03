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
 */

package freerails.move;

/**
 */
@SuppressWarnings("ALL")
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
     * @param trainA
     * @param trainB
     */
    public TrainCrashException(int trainA, int trainB) {
        this.trainA = trainA;
        this.trainB = trainB;
    }

    /**
     * @return
     */
    public int getTrainA() {
        return trainA;
    }

    /**
     * @return
     */
    public int getTrainB() {
        return trainB;
    }
}
