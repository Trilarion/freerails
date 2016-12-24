package jfreerails.world.player;

/**
 * FreerailsPrincipal that is a player in the game.
 * 
 * @author rob
 */
public class PlayerPrincipal extends FreerailsPrincipal {
    private static final long serialVersionUID = 3257563997099537459L;

    private final int id;

    private final String name;

    public PlayerPrincipal(int id, String name) {
        super(id);
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Player " + id;
    }

    /**
     * @return an integer unique to this PlayerPrincipal
     */
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlayerPrincipal)) {
            return false;
        }

        return id == ((PlayerPrincipal) o).id;
    }
}