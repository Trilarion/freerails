/*
  FreeRails 2 - A railroad strategy game Copyright (C) 2007 Roland Spatzenegger
  (c@npg.net)
  <p>
  This program is free software; you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation; either version 2 of the License, or (at your option) any later
  version.
  <p>
  This program is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  details.
  <p>
  You should have received a copy of the GNU General Public License along with
  this program; if not, write to the Free Software Foundation, Inc., 51
  Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package freerails.controller;

/**
 * throw this exception if a track is expected, but none was laid on the tile
 *
 * @author cymric
 */
public class NoTrackException extends RuntimeException {

    /**
     *
     */
    public NoTrackException() {
        super();
    }

    /**
     * @param message message
     * @param cause   cause
     */
    public NoTrackException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message message
     */
    public NoTrackException(String message) {
        super(message);
    }

    /**
     * @param cause cause
     */
    public NoTrackException(Throwable cause) {
        super(cause);
    }

}
