/*
 * TrainConsistListModel.java
 *
 * Created on 22 August 2003, 20:03
 */
package jfreerails.client.view;

import javax.swing.AbstractListModel;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.TypeID;
import jfreerails.world.train.TrainModel;


/**
 * AbstractListModel adaptor for a train schedule.
 * @author  Luke Lindsay
 */
public class TrainConsistListModel extends AbstractListModel {
    private final ReadOnlyWorld w;
    private final int trainNumber;
    private final FreerailsPrincipal principal;

    public TrainConsistListModel(ReadOnlyWorld w, int trainNumber,
        FreerailsPrincipal p) {
        this.w = w;
        this.trainNumber = trainNumber;
        principal = p;
    }

    /** Returns an Integer, a negative value represents an engine type; a positive value represents
     *a wagon type.
     */
    public Object getElementAt(int index) {
        TrainModel train = getTrain();

        if (0 == index) {
            return new TypeID(train.getEngineType(), SKEY.ENGINE_TYPES);
        }
		return new TypeID(train.getWagon(index - 1), SKEY.WAGON_TYPES);
    }

    public int getSize() {
        TrainModel train = getTrain();

        return train.getNumberOfWagons() + 1; //wagons + engine.
    }

    private TrainModel getTrain() {
        return (TrainModel)w.get(KEY.TRAINS, trainNumber, principal);
    }
}