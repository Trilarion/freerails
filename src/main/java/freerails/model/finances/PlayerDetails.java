package freerails.model.finances;

import org.jetbrains.annotations.NotNull;

/**
 * Stores the details a player that are shown on the leader board.
 */
public class PlayerDetails implements Comparable<PlayerDetails> {

    // TODO make not public
    public String name = "player";
    public Money networth = Money.ZERO;
    public int stations = 0;

    @Override
    public String toString() {
        return name + ", " + networth.toString() + " net worth, " + stations + "  stations.";
    }

    public int compareTo(@NotNull PlayerDetails o) {
        return networth.compareTo(o.networth);
    }

}
