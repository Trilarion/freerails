/*
 * Created on 15-Apr-2003
 * 
 */
package jfreerails.move;

import jfreerails.world.top.KEY;
import jfreerails.world.train.TrainModel;

/**
 * This Move adds a train to the train list.
 * @author Luke
 * 
 */
public class AddTrainMove extends AbstractAddItemToListMove {

	public AddTrainMove(int i, TrainModel train){		
		super(KEY.TRAINS, i, train);
	}

}
