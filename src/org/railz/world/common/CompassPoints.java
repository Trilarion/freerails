/*
 * Copyright (C) 2004 Robert Tuck
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

import org.railz.world.track.TrackTile;

/**
 * Defines some convenient bitmasks and methods for handling compass directions.
 * <p>
 * There are three different representations for direction:
 * <ul>
 * <li>An eight-bit representation which represents each of the 8 directions as
 * a single bit. This is the main internal representation of direction, and can
 * represent multiple combinations of directions by OR-ing components together.</li>
 * <li>A nine-bit representation which represents each of the 8 compass
 * directions with a single bit, plus a bit for the centre point, which is
 * currently unused. This is used to support legacy operations such as
 * transformation from file-names and xml.</li>
 * <li>A three-bit representation which represents a single compass direction as
 * ann integer between 0 and 7 inclusive. This is used in the pathfinder package
 * due to compactness.</li>
 * </ul>
 */
public final class CompassPoints implements FreerailsSerializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1974609151994911514L;
	/*
	 * 8-bit representations
	 */
	public static final byte NORTH = (byte) 0x80;
	public static final byte NORTHEAST = (byte) 0x40;
	public static final byte EAST = (byte) 0x20;
	public static final byte SOUTHEAST = (byte) 0x10;
	public static final byte SOUTH = (byte) 0x08;
	public static final byte SOUTHWEST = (byte) 0x04;
	public static final byte WEST = (byte) 0x02;
	public static final byte NORTHWEST = (byte) 0x01;

	/*
	 * 3-bit representation for a single compass point
	 */
	public static final byte NORTH3 = 0;
	public static final byte NORTHEAST3 = 1;
	public static final byte EAST3 = 2;
	public static final byte SOUTHEAST3 = 3;
	public static final byte SOUTH3 = 4;
	public static final byte SOUTHWEST3 = 5;
	public static final byte WEST3 = 6;
	public static final byte NORTHWEST3 = 7;

	public static int rotateClockwise3(int threeBit) {
		if (threeBit > 7)
			return 0;
		return threeBit++;
	}

	public static int rotateAnticlockwise3(int threeBit) {
		if (threeBit == 0)
			return 7;
		return threeBit--;
	}

	public static byte rotateClockwise(byte b) {
		int lsb = (b & 0x01);
		return (byte) (((b & 0xFF) >>> 1) | (lsb << 7));
	}

	public static byte rotateAnticlockwise(byte b) {
		int b2 = (b << 1);
		b2 |= ((b2 & 0x100) >>> 8);
		return (byte) b2;
	}

	public static byte invert(byte b) {
		for (int i = 0; i < 4; i++) {
			b = rotateClockwise(b);
		}
		return b;
	}

	/**
	 * @param threeBit
	 *            the three-bit representation of a compass point
	 * @return the three-bit representation of the opposite direction
	 */
	public static int invert3(int threeBit) {
		switch (threeBit) {
		case NORTH3:
			return SOUTH3;
		case NORTHEAST3:
			return SOUTHWEST3;
		case EAST3:
			return WEST3;
		case SOUTHEAST3:
			return NORTHWEST3;
		case SOUTH3:
			return NORTH3;
		case SOUTHWEST3:
			return NORTHEAST3;
		case WEST3:
			return EAST3;
		case NORTHWEST3:
			return SOUTHEAST3;
		}
		throw new IllegalArgumentException();
	}

	/**
	 * @param directions
	 *            array of size 8 where elements will be set to true if the
	 *            corresponding three-bit direction is a component of the
	 *            eight-bit bitmask.
	 */
	public static void getThreeBitComponents(byte eightBits,
			boolean[] directions) {
		int bit = 0x80;
		for (int i = 0; i < 7; i++) {
			directions[i] = (eightBits & bit) != 0;
		}
	}

	/**
	 * @param b
	 *            a direction representing a single compass point.
	 * @return the three-bit representation of b.
	 */
	public static int eightBitToThreeBit(byte b) {
		switch (b) {
		case NORTH:
			return NORTH3;
		case NORTHEAST:
			return NORTHEAST3;
		case EAST:
			return EAST3;
		case SOUTHEAST:
			return SOUTHEAST3;
		case SOUTH:
			return SOUTH3;
		case SOUTHWEST:
			return SOUTHWEST3;
		case WEST:
			return WEST3;
		case NORTHWEST:
			return NORTHWEST3;
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * @param s
	 *            a 9 bit binary number which encodes a track template. A 1
	 *            indicates the presence of track. Bit 8(MSB) 7 6 5 4 3 2 1
	 *            0(LSB) NW N NE W N/A E SW S SE
	 */
	public static byte nineBitToEightBit(String s) {
		int nineBits = Integer.parseInt(s, 2);
		byte eightBits = 0;
		if ((nineBits & 0x100) > 0)
			eightBits |= NORTHWEST;
		if ((nineBits & 0x080) > 0)
			eightBits |= NORTH;
		if ((nineBits & 0x040) > 0)
			eightBits |= NORTHEAST;
		if ((nineBits & 0x020) > 0)
			eightBits |= WEST;
		if ((nineBits & 0x008) > 0)
			eightBits |= EAST;
		if ((nineBits & 0x004) > 0)
			eightBits |= SOUTHWEST;
		if ((nineBits & 0x02) > 0)
			eightBits |= SOUTH;
		if ((nineBits & 0x01) > 0)
			eightBits |= SOUTHEAST;
		return eightBits;
	}

	public static String toAbrvString(int threeBit) {
		switch (threeBit) {
		case NORTH3:
			return "n";
		case NORTHEAST3:
			return "ne";
		case EAST3:
			return "e";
		case SOUTHEAST3:
			return "se";
		case SOUTH3:
			return "s";
		case SOUTHWEST3:
			return "sw";
		case WEST3:
			return "w";
		case NORTHWEST3:
			return "nw";
		default:
			throw new IllegalArgumentException();
		}
	}

	public static String toString3(int threeBit) {
		switch (threeBit) {
		case NORTH3:
			return "North";
		case NORTHEAST3:
			return "Northeast";
		case EAST3:
			return "East";
		case SOUTHEAST3:
			return "SouthEast";
		case SOUTH3:
			return "South";
		case SOUTHWEST3:
			return "Southwest";
		case WEST3:
			return "West";
		case NORTHWEST3:
			return "Northwest";
		default:
			throw new IllegalArgumentException();
		}
	}

	private static final int DIAGONAL_DELTAS = (int) (Math.sqrt(2.0) * TrackTile.DELTAS_PER_TILE);

	private static final int STRAIGHT_DELTAS = TrackTile.DELTAS_PER_TILE;

	/**
	 * @return the length of a straight/diagonal vector across a tile measured
	 *         in Deltas
	 */
	public static int getLength(byte direction) {
		switch (direction) {
		case NORTH:
		case EAST:
		case WEST:
		case SOUTH:
			return STRAIGHT_DELTAS;
		case NORTHEAST:
		case NORTHWEST:
		case SOUTHEAST:
		case SOUTHWEST:
			return DIAGONAL_DELTAS;
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * @return the length of a straight/diagonal vector across a tile measured
	 *         in Deltas
	 */
	public static int getLength3(int threeBits) {
		switch (threeBits) {
		case NORTH3:
		case EAST3:
		case SOUTH3:
		case WEST3:
			return STRAIGHT_DELTAS;
		case NORTHEAST3:
		case SOUTHEAST3:
		case SOUTHWEST3:
		case NORTHWEST3:
			return DIAGONAL_DELTAS;
		}
		throw new IllegalArgumentException();
	}

	public static int getUnitDeltaX(byte direction) {
		switch (direction) {
		case NORTH:
		case SOUTH:
			return 0;
		case NORTHEAST:
		case EAST:
		case SOUTHEAST:
			return 1;
		case NORTHWEST:
		case WEST:
		case SOUTHWEST:
			return -1;
		default:
			throw new IllegalArgumentException();
		}
	}

	public static int getUnitDeltaY(byte direction) {
		switch (direction) {
		case EAST:
		case WEST:
			return 0;
		case NORTHWEST:
		case NORTH:
		case NORTHEAST:
			return -1;
		case SOUTHWEST:
		case SOUTH:
		case SOUTHEAST:
			return 1;
		default:
			throw new IllegalArgumentException();
		}
	}

	public static byte unitDeltasToDirection(int dx, int dy) {
		switch (dx) {
		case -1:
			switch (dy) {
			case -1:
				return NORTHWEST;
			case 0:
				return WEST;
			case 1:
				return SOUTHWEST;
			default:
				throw new IllegalArgumentException();
			}
		case 0:
			switch (dy) {
			case -1:
				return NORTH;
			case 1:
				return SOUTH;
			default:
				throw new IllegalArgumentException();
			}
		case 1:
			switch (dy) {
			case -1:
				return NORTHEAST;
			case 0:
				return EAST;
			case 1:
				return SOUTHEAST;
			default:
				throw new IllegalArgumentException();
			}
		default:
			throw new IllegalArgumentException();
		}
	}

	public static String toString(byte direction) {
		String s = "";
		int mask = 1;
		for (int i = 0; i < 8; i++) {
			byte b = (byte) (direction & mask);
			if (b != 0) {
				if (s.length() > 0)
					s += " | ";
				s += toAbrvString(eightBitToThreeBit(b));
			}
			mask = mask << 1;
		}
		return s;
	}
}
