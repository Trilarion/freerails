/*
 * Created on 10-Mar-2004
 *
 */
package jfreerails.world.common;

/**
 * This class represents actual game speed. If the game speed <code>speed</code>
 * is lesser then zero, game is paused. After unpausing, the speed should be
 * <code>-speed</code>.
 * 
 * I.e. pausing/unpausing is equal to multiply the speed by -1.
 * 
 * @author MystiqueAgent
 * 
 */
public class GameSpeed implements FreerailsSerializable {
    private static final long serialVersionUID = 3257562901983081783L;

    private final int speed;

    @Override
    public String toString() {
        return "GameSpeed:" + String.valueOf(speed);
    }

    public GameSpeed(int speed) {
        this.speed = speed;
    }

    public int getSpeed() {
        return speed;
    }

    public boolean isPaused() {
        return speed < 1;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GameSpeed) {
            GameSpeed test = (GameSpeed) o;

            return this.speed == test.speed;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return speed;
    }
}