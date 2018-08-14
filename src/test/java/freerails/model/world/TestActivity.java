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

package freerails.model.world;

import freerails.model.activity.Activity;

import java.io.Serializable;

/**
 *
 */
public class TestActivity implements Activity {

    private static final long serialVersionUID = 1298936498785131183L;

    private final double duration;

    /**
     * @param duration
     */
    public TestActivity(int duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof TestActivity))
            return false;

        final TestActivity testActivity = (TestActivity) obj;

        return !(duration != testActivity.duration);
    }

    @Override
    public int hashCode() {
        return (int) duration;
    }

    /**
     * @return
     */
    public double duration() {
        return duration;
    }

    /**
     * @param time
     * @return
     */
    public Serializable getStateAtTime(double time) {
        return new TestState((int) time);
    }

    @Override
    public String toString() {
        return getClass().getName() + '{' + duration + '}';
    }
}
