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

/*
 *
 */
package freerails.model.game;

import freerails.util.Identifiable;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

/**
 * Represents the state of the economy.
 */
public class Sentiment extends Identifiable {

    @Serial
    private static final long serialVersionUID = 3834025840475321136L;
    private final String name;
    private final double rate;

    /**
     * Creates an economic sentiment with its stable identifier and rate.
     *
     * @param id stable sentiment identifier
     * @param name display name
     * @param rate sentiment rate used by the game economy
     */
    public Sentiment(int id, @NotNull String name, double rate) {
        super(id);
        this.name = name;
        this.rate = rate;
    }

    /** Returns the display name of this sentiment. */
    public String getName() {
        return name;
    }

    /**
     * @return the sentiment's rate
     */
    public double getRate() {
        return rate;
    }
}