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
package org.railz.world.train;

import java.util.*;

import org.railz.world.common.*;
import org.railz.world.top.*;

/**
 * Describes the position of head of the train as a function of time. The
 * current model assumes speed as a linear function of time (constant
 * acceleration).
 */
public class TrainPathFunction implements FreerailsSerializable {
    private final TrainPathSegment[] segments;
    private final int tBase;
    
    public TrainPathFunction(int tBase, ArrayList segmentArrayList) {
	this.tBase = tBase;
	segments = (TrainPathSegment [] ) segmentArrayList.toArray
	    (new TrainPathSegment[segmentArrayList.size()]);
    }
    
    public TrainPathFunction(TrainPathFunction tpf) {
	segments = tpf.segments;
	tBase = tpf.tBase;
    }

    public float getSpeed(int t) {
	t -= tBase;
	for (int i = 0; i < segments.length; i++) {
	    if (segments[i].tMax >= (float) t)
		return segments[i].getSpeed((float) t);
	}
	return segments[segments.length - 1].getSpeed
	    (segments[segments.length - 1].tMax);
    }

    public float getDistance(int t) {
	t -= tBase;
	for (int i = 0; i < segments.length; i++) {
	    if (segments[i].tMax >= (float) t)
		return segments[i].getDistance((float) t);
	}
	return segments[segments.length -
	    1].getDistance(segments[segments.length - 1].tMax);
    }

    public static class TrainPathSegment implements FreerailsSerializable {
	/** initial time at the start of this segment */
	private final float t0;
	/** initial velocity at the start of this segment */
	private final float v0;
	/** acceleration during this segment */
	private final float a;
	/** distance at t0 */
	private final float s0;
	/** max time for which this segment is valid */
	private final float tMax;

	/** @return -1.0f if we never reach sMax */
	public float getTMax() {
	    return tMax;
	}

	public float getSpeed(float t) {
	    return v0 + (t - t0) * a;
	}

	public float getDistance(float t) {
	    float dt = t - t0;
	    // s = ut + at^2 / 2
	    // at^2 / 2 + ut - s = 0
	    // quadratic in t
	    // a = a / 2, b = v0, c = -(s - s0)
	    return s0 + (v0 * dt) + (a * dt * dt) / 2;
	}

	public TrainPathSegment(float t0, float v0, float a, float s0, float
		sMax) {
	    this.t0 = t0;
	    this.v0 = v0;
	    // when a is small, approximate to 0 to avoid problems with
	    // taking the difference between two small numbers
	    if (Math.abs(a) > 0.0001)
		this.a = a;
	    else
		this.a = 0.0f;
	    this.s0 = s0;
	    float deltaS = sMax - s0;
	    // (-b +- sqrt(b^2 - 4ac)) / 2a
	    if (a == 0.0f) {
		tMax = t0 + (sMax - s0) / v0;
	    } else if (v0 * v0 + 2.0f * a * deltaS < 0.0f) {
		tMax = -1.0f;
	    } else {
		tMax = t0 + (-v0 + (float) Math.sqrt(v0 * v0 + 2.0f * a * 
			    deltaS)) / a;
	    }
	}

	public String toString() {
	    return "Segment: t0=" + t0 + ", v0=" + v0 + ", a=" + a +
		", s0=" + s0 + ", tMax=" + tMax;
	}

	public float getAcceleration() {
	    return a;
	}
    }

    public String toString() {
	String s = "TrainPathFunction: ";
	s += "tBase=" + tBase + ", ";
	for (int i = 0; i < segments.length; i++) {
	    if (i > 0)
		s += ", ";
	    s += segments[i].toString();
	}
	return s;
    }
}
