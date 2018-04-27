package freerails.model.world;

import freerails.model.player.FreerailsPrincipal;

/**
 *
 */
public class WorldUtils {

    private WorldUtils() {
    }

    /**
     * Gets the player index in the world.
     *
     * @param world
     * @param principal
     * @return
     */
    public static int getPlayerIndex(ReadOnlyWorld world, FreerailsPrincipal principal) {
        for (int i = 0; i < world.getNumberOfPlayers(); i++) {
            if (world.getPlayer(i).getPrincipal().equals(principal)) {
                return i;
            }
        }

        throw new IllegalStateException();
    }
}
