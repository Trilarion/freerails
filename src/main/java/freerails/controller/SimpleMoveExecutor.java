package freerails.controller;

import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.World;

/**
 * A MoveExecutor that executes moves on the world object passed to its
 * constructor.
 * 
 * @author Luke
 * 
 */
public class SimpleMoveExecutor implements MoveExecutor {

    private final World w;

    private final FreerailsPrincipal p;

    public SimpleMoveExecutor(World world, int playerID) {
        w = world;
        Player player = w.getPlayer(playerID);
        p = player.getPrincipal();
    }

    public MoveStatus doMove(Move m) {
        return m.doMove(w, p);
    }

    public MoveStatus doPreMove(PreMove pm) {
        Move m = pm.generateMove(w);
        return m.doMove(w, p);
    }

    public MoveStatus tryDoMove(Move m) {
        return m.tryDoMove(w, p);
    }

    public ReadOnlyWorld getWorld() {
        return w;
    }

    public FreerailsPrincipal getPrincipal() {
        return p;
    }

}
