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
 * Groups the model state for transport operations: train consists, schedules,
 * activities and motion, station cargo queues, and cargo handling. Independently
 * useful APIs are organized in the {@code train}, {@code station}, and
 * {@code cargo} subpackages. Moves that change this state remain in
 * {@link freerails.move}.
 */
package freerails.model.operations;
