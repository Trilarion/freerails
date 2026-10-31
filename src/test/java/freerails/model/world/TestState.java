/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import java.io.Serializable;

/**
 * Provides test support for state.
 */
public class TestState implements Serializable {

    private static final long serialVersionUID = 5122023949873919060L;

    /** The value used to test world-state serialization and equality. */
    private final int x;

    /**
     * Creates an instance of test state.
     * @param x the x to use in the test.
     */
    public TestState(int x) {
        this.x = x;
    }

    /**
     * Performs equals.
     * @param obj the obj to use in the test.
     * @return the result of equals.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof TestState))
            return false;

        final TestState testState = (TestState) obj;

        return x == testState.x;
    }

    /**
     * Indicates whether h code is present.
     * @return whether h code is present.
     */
    @Override
    public int hashCode() {
        return x;
    }
}
