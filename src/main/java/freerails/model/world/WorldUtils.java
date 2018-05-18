package freerails.model.world;

import freerails.model.player.Player;

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
    public static int getPlayerIndex(UnmodifiableWorld world, Player principal) {
        for (int i = 0; i < world.getNumberOfPlayers(); i++) {
            if (world.getPlayer(i).equals(principal)) {
                return i;
            }
        }

        throw new IllegalStateException();
    }
}
