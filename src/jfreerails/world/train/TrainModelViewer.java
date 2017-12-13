/*
 * Copyright (C) Robert Tuck
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

/**
 * @author rtuck99@users.berlios.de
 */
package jfreerails.world.train;

import java.util.GregorianCalendar;
import jfreerails.world.common.*;
import jfreerails.world.player.*;
import jfreerails.world.top.*;

public class TrainModelViewer implements FixedAsset {
    private ReadOnlyWorld world;
    private TrainModel trainModel;
    private GameCalendar calendar;

    public TrainModelViewer(ReadOnlyWorld w) {
	world = w;
	calendar = (GameCalendar) world.get(ITEM.CALENDAR,
		    Player.AUTHORITATIVE);
    }

    public void setTrainModel(TrainModel tm) {
	trainModel = tm;
    }

    /**
     * The asset value is calculated at initially 90% of the list price
     * and decreases linearly over 25 years to 10% of the initial price.
     * @return the asset value of the train.
     */
    public long getBookValue() {
	GameTime now = (GameTime) world.get(ITEM.TIME, Player.AUTHORITATIVE);
	long nowMillis = calendar.getCalendar(now).getTimeInMillis();
	long creationMillis = calendar.getCalendar
	    (trainModel.getCreationDate()).getTimeInMillis();
	/* assume years are 365 days long for simplicity */
	int elapsedYears = (int) 
	    ((nowMillis - creationMillis) / (1000L * 60 * 60 * 24 * 365));
	long initialPrice = ((EngineType)
		    world.get(KEY.ENGINE_TYPES, trainModel.getEngineType(),
			Player.AUTHORITATIVE)).getPrice();
	if (elapsedYears >= 25) {
	    return (long) (initialPrice * 0.10);
	}
	return (long) (initialPrice * (0.90 - (elapsedYears * 0.8 / 25)));
    }

    /**
     * @return the amount of maintenance charged per month
     */
    public long getMaintenance() {
	return ((EngineType) world.get(KEY.ENGINE_TYPES,
		    trainModel.getEngineType(),
		    Player.AUTHORITATIVE)).getMaintenance() / 12;
    }
}
