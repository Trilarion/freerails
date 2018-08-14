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

package freerails.model.activity;

import freerails.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.NoSuchElementException;

// TODO what about a standard list iterator? need a bidirectional iterator, but not hard to write a generic one
/**
 *
 */
public class ActivityIterator {

    /**
     *
     */
    private final List<Pair<Activity, Double>> activities;

    /**
     *
     */
    private int currentIndex;
    private Pair<Activity, Double> currentActivityWithTime;

    /**
     */
    public ActivityIterator(@NotNull List<Pair<Activity, Double>> activities) {
        this.activities = activities;
        currentIndex = 0;
        currentActivityWithTime = this.activities.get(currentIndex);
    }

    /**
     * @return
     */
    public Activity getActivity() {
        return currentActivityWithTime.getA();
    }

    public double getStartTime() {
        return currentActivityWithTime.getB();
    }

    /**
     * @return
     */
    public boolean hasNext() {
        return (currentIndex + 1) < activities.size();
    }

    /**
     *
     */
    public void nextActivity() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        currentIndex++;
        currentActivityWithTime = activities.get(currentIndex);
    }

    /**
     *
     */
    public void gotoLastActivity() {
        currentIndex = activities.size() - 1;
        currentActivityWithTime = activities.get(currentIndex);
    }

    /**
     * @return
     */
    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    /**
     * @throws NoSuchElementException
     */
    public void previousActivity() throws NoSuchElementException {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        currentIndex--;
        currentActivityWithTime = activities.get(currentIndex);
    }

}
