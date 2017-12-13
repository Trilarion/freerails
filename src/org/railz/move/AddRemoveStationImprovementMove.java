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

package org.railz.move;

import java.util.*;

import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.station.*;
import org.railz.world.top.*;
import org.railz.world.accounts.*;

public class AddRemoveStationImprovementMove extends CompositeMove {
    public static AddRemoveStationImprovementMove
	generateAddImprovementMove(ReadOnlyWorld w, ObjectKey stationKey,
		ObjectKey improvementKey)
	{
	    StationModel before = (StationModel) w.get(KEY.STATIONS,
		    stationKey.index, stationKey.principal);

	    // determine whether we can build this improvement
	    StationModelViewer smv = new StationModelViewer(w);
	    smv.setStationModel(before);
	    if (!smv.canBuildImprovement(improvementKey.index))
		return null;

	    int[] improvements = before.getImprovements();
	    ArrayList tmp = new ArrayList();
	    StationImprovement si = (StationImprovement)
		w.get(KEY.STATION_IMPROVEMENTS, improvementKey.index,
			Player.AUTHORITATIVE);
	    int[] replaced = si.getReplacedImprovements();
	    for (int i = 0; i < improvements.length; i++)
		tmp.add(new Integer(improvements[i]));

	    tmp.add(new Integer(improvementKey.index));

	    for (int i = 0; i < replaced.length; i++) {
		if (tmp.contains(new Integer(replaced[i]))) {
			tmp.remove(new Integer(replaced[i]));
		}
	    }
	    int[] newImprovements = new int[tmp.size()];
	    for (int i = 0; i < newImprovements.length; i++)
		newImprovements[i] = ((Integer) tmp.get(i)).intValue();

	    StationModel after = (StationModel)
		before.setImprovements(newImprovements);

	    Move changeStationMove = new ChangeStationMove(stationKey.index,
		    before, after, stationKey.principal);

	    // generate transaction
	    GameTime now = (GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE);
	    Transaction t = new AddItemTransaction(now,
		    AddItemTransaction.BUILDING, 0, 1,
		    - smv.getImprovementCost(improvementKey.index));

	    Move transactionMove = new AddTransactionMove(0, t,
		    stationKey.principal);
	    return new AddRemoveStationImprovementMove
		(new Move[] {changeStationMove, transactionMove});
	}

    public static AddRemoveStationImprovementMove
	generateRemoveImprovementMove(ReadOnlyWorld w,
		ObjectKey stationKey, ObjectKey improvementKey) {
	    StationModel before = (StationModel) w.get(KEY.STATIONS,
		    stationKey.index, stationKey.principal);

	    // determine whether we can build this improvement
	    StationModelViewer smv = new StationModelViewer(w);
	    smv.setStationModel(before);

	    int[] improvements = before.getImprovements();
	    ArrayList tmp = new ArrayList();
	    for (int i = 0; i < improvements.length; i++)
		tmp.add(new Integer(improvements[i]));

	    if (!tmp.contains(new Integer(improvementKey.index)))
		return null;

	    tmp.remove(new Integer(improvementKey.index));
	    int[] newImprovements = new int[tmp.size()];
	    for (int i = 0; i < newImprovements.length; i++) 
		newImprovements[i] = ((Integer) tmp.get(i)).intValue();

	    StationModel after = (StationModel)
		before.setImprovements(newImprovements);

	    Move changeStationMove = new ChangeStationMove(stationKey.index,
		    before, after, stationKey.principal);

	    StationImprovement si = (StationImprovement)
		w.get(KEY.STATION_IMPROVEMENTS, improvementKey.index,
			Player.AUTHORITATIVE);
	    
	    return new AddRemoveStationImprovementMove
		(new Move[] {changeStationMove});
	}

    private AddRemoveStationImprovementMove(Move[] moves) {
	super(moves);
    }
}
