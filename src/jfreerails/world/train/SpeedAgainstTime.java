/*
 * Created on 10-Jul-2005
 *
 */
package jfreerails.world.train;

import jfreerails.world.common.FreerailsSerializable;

public interface SpeedAgainstTime extends FreerailsSerializable {

	double calcS(double t);

	double calcT(double s);

	double calcV(double t);

	double calcA(double t);

	double getT();

	double getS();

}