/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.railz.world.common;

public final class PathLength implements FreerailsSerializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7419124916261249701L;
	public int straightLength;
	public int diagLength;

	private static final double ROOT_TWO = Math.sqrt(2.0);

	public PathLength(int x1, int y1, int x2, int y2) {
		if (x1 == x2) {
			straightLength = (y1 < y2) ? (y2 - y1) : (y1 - y2);
			diagLength = 0;
		} else if (y1 == y2) {
			straightLength = (x1 < x2) ? (x2 - x1) : (x1 - x2);
			diagLength = 0;
		} else {
			diagLength = (y1 < y2) ? (y2 - y1) : (y1 - y2);
			straightLength = 0;
		}
	}

	public PathLength() {
		straightLength = 0;
		diagLength = 0;
	}

	public PathLength(PathLength pl) {
		straightLength = pl.straightLength;
		diagLength = pl.diagLength;
	}

	public PathLength(int s, int d) {
		straightLength = s;
		diagLength = d;
		assert d > 0 && s > 0;
	}

	public void add(PathLength pl) {
		straightLength += pl.straightLength;
		diagLength += pl.diagLength;
	}

	public void subtract(PathLength pl) {
		straightLength -= pl.straightLength;
		diagLength -= pl.diagLength;
		assert (straightLength >= 0 && diagLength >= 0);
	}

	public double getLength() {
		return straightLength + ROOT_TWO * diagLength;
	}

	public void setLength(double l) {
		assert (straightLength == 0 || diagLength == 0);
		if (straightLength != 0) {
			straightLength = (int) l;
		} else {
			diagLength = (int) (l / ROOT_TWO);
		}
	}

	public void setLength(PathLength pl) {
		straightLength = pl.straightLength;
		diagLength = pl.diagLength;
	}

	@Override
	public String toString() {
		return "PathLength(s = " + straightLength + ", d = " + diagLength + ")";
	}
}
