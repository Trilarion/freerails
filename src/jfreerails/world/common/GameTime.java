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
	
	private final long time;
	
	public GameTime(long l){
		this.time = l;
	}
	
		
	public long getTime() {
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
