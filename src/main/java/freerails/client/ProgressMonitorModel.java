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
 * ProgressMonitorModel.java
 *
 */
package freerails.client;

/**
 * This interface defines callbacks that can be used to let the user know how a
 * slow task is progressing.
 */
public interface ProgressMonitorModel {

    /**
     *
     */
    ProgressMonitorModel EMPTY = new ProgressMonitorModel() {

        public void setValue(int i) {}

        public void nextStep(int max) { }

        public void finished() {}
    };

    /**
     * @param i
     */
    void setValue(int i);

    /**
     * @param max
     */
    void nextStep(int max);

    /**
     *
     */
    void finished();
}