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
package freerails.world;

import java.io.Serializable;
import java.util.NoSuchElementException;

// TODO this activity iterator can do far too much, use a standard iterator instead and put the extra features into an activity
/**
 *
 */
public interface ActivityIterator {

    /**
     * @returnhh
     */
    boolean hasNext();

    /**
     * @throws NoSuchElementException
     */
    void nextActivity() throws NoSuchElementException;

    /**
     * Returns the time the current activity starts.
     *
     * @return
     */
    double getStartTime();

    /**
     * Returns the time the current activity ends.
     *
     * @return
     */
    double getFinishTime();

    /**
     * @return
     */
    double getDuration();

    /**
     * Converts an absolute time value to a time value relative to the start of
     * the current activity. If absoluteTime is greater then getFinishTime(), getDuration() is
     * returned.
     *
     * @param absoluteTime
     * @return
     */
    @SuppressWarnings("unused")
    double absoluteToRelativeTime(double absoluteTime);

    /**
     * @param absoluteTime
     * @return
     */
    Serializable getState(double absoluteTime);

    /**
     * @return
     */
    Activity getActivity();

    /**
     *
     */
    void gotoLastActivity();

    /**
     * @throws NoSuchElementException
     */
    void previousActivity() throws NoSuchElementException;

    /**
     * @return
     */
    boolean hasPrevious();
}
