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

/**
 * Provides general-purpose data structures and helper methods shared across
 * the application. Utilities include collection and serialization helpers,
 * identified values, pairs, integer geometry types such as {@link Vec2D} and
 * {@link Segment}, and iterator support.
 *
 * <p>Code in this package should not depend on other FreeRails packages, so
 * these utilities remain reusable throughout the application.
 *
 * @see <a href="https://github.com/Trilarion/freerails/blob/master/docs/development.rst">Package responsibilities and contracts</a>
 */
package freerails.util;