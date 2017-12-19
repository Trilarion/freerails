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

package freerails.network;

/**
 * Synchronized flag - used to tell threads whether they should keep going.
 * Note, thought about using volatile keyword but wasn't sure if it is
 * implemented on all JVMs
 */
class SynchronizedFlag {
    private boolean isOpen = true;

    SynchronizedFlag(boolean b) {
        this.isOpen = b;
    }

    public synchronized boolean isOpen() {
        return isOpen;
    }

    public synchronized void close() {
        this.isOpen = false;
        notifyAll();
    }

    public synchronized void open() {
        this.isOpen = true;
        notifyAll();
    }
}