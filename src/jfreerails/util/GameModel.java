package jfreerails.util;

/**
 * Defines a standard method to update the game world.
 * @author Luke
 *
 */
public interface GameModel {
    public static final GameModel NULL_MODEL = new GameModel() {
            public void update() {
            }
        };

    void update();
}