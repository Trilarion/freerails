package freerails.controller;

import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.world.common.ImPoint;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;
import freerails.world.track.TrackRule;
import org.apache.log4j.Logger;

import java.util.NoSuchElementException;

/**
 * Class to build a station at a given point, names station after nearest city.
 * If that name is taken then a "Junction" or "Siding" is added to the name.
 *
 *
 * Updated 12th April 2003 by Scott Bennett to include nearest city names.
 */
public class StationBuilder {
    private static final Logger logger = Logger.getLogger(StationBuilder.class
            .getName());
    private final MoveExecutor executor;
    private int ruleNumber;

    /**
     *
     * @param executor
     */
    public StationBuilder(MoveExecutor executor) {
        this.executor = executor;

        TrackRule trackRule;

        int i = -1;

        ReadOnlyWorld world = executor.getWorld();

        do {
            i++;
            trackRule = (TrackRule) world.get(SKEY.TRACK_RULES, i);
        } while (!trackRule.isStation());

        ruleNumber = i;
    }

    /**
     *
     * @param p
     * @return
     */
    public MoveStatus tryBuildingStation(ImPoint p) {
        ReadOnlyWorld world = executor.getWorld();

        FreerailsPrincipal principal = executor.getPrincipal();
        AddStationPreMove preMove = AddStationPreMove.newStation(p,
                this.ruleNumber, principal);
        Move m = preMove.generateMove(world);

        return executor.tryDoMove(m);
    }

    /**
     *
     * @param p
     * @return
     */
    public MoveStatus buildStation(ImPoint p) {
        // Only build a station if there is track at the specified point.
        MoveStatus status = tryBuildingStation(p);
        if (status.ok) {
            FreerailsPrincipal principal = executor.getPrincipal();
            AddStationPreMove preMove = AddStationPreMove.newStation(p,
                    this.ruleNumber, principal);
            return executor.doPreMove(preMove);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(status.message);
        }
        return status;
    }

    /**
     *
     * @param ruleNumber
     */
    public void setStationType(int ruleNumber) {
        this.ruleNumber = ruleNumber;
    }

    int getTrackTypeID(String string) {
        ReadOnlyWorld w = executor.getWorld();
        for (int i = 0; i < w.size(SKEY.TRACK_RULES); i++) {
            TrackRule r = (TrackRule) w.get(SKEY.TRACK_RULES, i);

            if (string.equals(r.getTypeName())) {
                return i;
            }
        }
        throw new NoSuchElementException();
    }
}