/*
 * Created on 01-Jun-2003
 *
 */
package jfreerails.world.common;


/**This class represents a specific instant in time during a game.
 *
 * @author Luke
 *
 */
public class GameTime implements FreerailsSerializable {
    /** The first possible time.*/
    public static final GameTime BIG_BANG = new GameTime(Integer.MIN_VALUE);

    /** The last possible time.*/
    public static final GameTime END_OF_THE_WORLD = new GameTime(Integer.MAX_VALUE);
    private final int time;

    public String toString() {
        return "GameTime:" + String.valueOf(time);
    }

    public int hashCode() {
        return time;
    }

    public GameTime(int l) {
        this.time = l;
    }

    public GameTime nextTick() {
        return new GameTime(time + 1);
    }

    public int getTime() {
        return time;
    }

    public boolean equals(Object o) {
        if (o instanceof GameTime) {
            GameTime test = (GameTime)o;

            return this.time == test.time;
        } else {
            return false;
        }
    }
}