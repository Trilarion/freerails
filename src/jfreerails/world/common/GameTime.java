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
	
	private final int time;
	
	public String toString() {
	    return "GameTime:" + String.valueOf(time);
	}

	public GameTime(int l){
		this.time = l;
	}
	
		
	public int getTime() {
		return time;
	}
	
	public boolean equals(Object o) {		
		if(o instanceof GameTime){
			GameTime test = (GameTime)o;
			return this.time == test.time;
		}else{		
			return false;
		}
	}

}
