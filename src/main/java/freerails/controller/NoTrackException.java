/**
 * FreeRails 2 - A railroad strategy game Copyright (C) 2007 Roland Spatzenegger
 * (c@npg.net)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA. 
 */

package freerails.controller;

/**
 * throw this exception if a track is expected, but none was laid on the tile
 * 
 * @author cymric
 * @version $Revision 1.1$
 */
public class NoTrackException extends RuntimeException {

    /**
     * 
     */
    public NoTrackException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public NoTrackException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public NoTrackException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public NoTrackException(Throwable cause) {
        super(cause);
    }

}
