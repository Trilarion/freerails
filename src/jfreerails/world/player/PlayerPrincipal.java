package jfreerails.world.player;


/**
 * FreerailsPrincipal that is a player in the game.
 * @author rob
 */
public class PlayerPrincipal extends FreerailsPrincipal {
    private final int id;

    public PlayerPrincipal(int id) {
        this.id = id;
    }

    public String getName() {
        return "Player " + id;
    }

    public int hashCode() {
        return id;
    }

    public String toString() {
        return "Player " + id;
    }

    /**
     * @return an integer unique to this PlayerPrincipal
     */
    public int getId() {
        return id;
    }

    public boolean equals(Object o) {
        if (!(o instanceof PlayerPrincipal)) {
            return false;
        }

        return id == ((PlayerPrincipal)o).id;
    }
}