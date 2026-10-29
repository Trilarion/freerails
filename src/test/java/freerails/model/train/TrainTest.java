/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the FreeRails Team, either version 3 of the License, or
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

package freerails.model.train;

import freerails.model.train.activity.Activity;
import junit.framework.TestCase;

public class TrainTest extends TestCase {

    public void testActivitiesStartNoEarlierThanPreviousCompletion() {
        Train train = new Train(1, 1);
        TestActivity first = new TestActivity(10, 5);
        TestActivity delayed = new TestActivity(12, 4);
        TestActivity later = new TestActivity(30, 2);
        TestActivity delayedAfterLater = new TestActivity(31, 1);

        train.addActivity(first);
        train.addActivity(delayed);
        train.addActivity(later);
        train.addActivity(delayedAfterLater);

        assertEquals(10.0, first.getStartTime(), 0.0);
        assertEquals(15.0, delayed.getStartTime(), 0.0);
        assertEquals(30.0, later.getStartTime(), 0.0);
        assertEquals(32.0, delayedAfterLater.getStartTime(), 0.0);
    }

    private static final class TestActivity extends Activity<String> {

        private final double duration;

        private TestActivity(double proposedStartTime, double duration) {
            this.duration = duration;
            setStartTime(proposedStartTime);
        }

        @Override
        public double getDuration() {
            return duration;
        }

        @Override
        public String getStateAtTime(double time) {
            return "";
        }
    }
}
