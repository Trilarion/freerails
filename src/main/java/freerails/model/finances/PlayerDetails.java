package freerails.model.finances;

import org.jetbrains.annotations.NotNull;

/**
 * Stores the details a player that are shown on the leader board.
 */
public class PlayerDetails implements Comparable<PlayerDetails> {

    // TODO set in constructor?
    private String name = "player";
    private Money networth = Money.ZERO;
    private int stations = 0;

    @Override
    public String toString() {
        return name + ", " + networth.toString() + " net worth, " + stations + "  stations.";
    }

    public int compareTo(@NotNull PlayerDetails o) {
        return networth.compareTo(o.networth);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Money getNetworth() {
        return networth;
    }

    public void setNetworth(Money networth) {
        this.networth = networth;
    }

    public int getStations() {
        return stations;
    }

    public void setStations(int stations) {
        this.stations = stations;
    }
}
