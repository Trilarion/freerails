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
package freerails.model.game;

import freerails.model.Identifiable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the state of the economy.
 */
public class Sentiment extends Identifiable {

    private static final long serialVersionUID = 3834025840475321136L;
    private final String name;
    private final double rate;

    public Sentiment(int id, @NotNull String name, double rate) {
        super(id);
        this.name = name;
        this.rate = rate;
    }

    public String getName() {
        return name;
    }

    /**
     * @return
     */
    public double getRate() {
        return rate;
    }
}