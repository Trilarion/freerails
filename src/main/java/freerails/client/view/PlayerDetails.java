package freerails.client.view;

import freerails.world.finances.Money;

/**
 * Stores the details a player that are shown on the leader board.
 */
class PlayerDetails implements Comparable<PlayerDetails> {

    String name = "player";

    Money networth = new Money(0);

    int stations = 0;

    @Override
    public String toString() {
        return name +
                ", " +
                networth.toString() +
                " net worth, " +
                stations +
                "  stations.";
    }

    public int compareTo(PlayerDetails o) {
        long l = o.networth.getAmount() - networth.getAmount();
        return (int) l;
    }

}
