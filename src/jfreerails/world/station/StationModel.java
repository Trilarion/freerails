package jfreerails.world.station;

import jfreerails.world.common.FreerailsSerializable;


public class StationModel implements FreerailsSerializable {
	
	public int x;
	public int y;
	
	public StationModel(int x, int y){
		this.x =x;
		this.y =y;
		
	}
	public StationModel(){
		x=0;
		y=0;	
	}
	
}