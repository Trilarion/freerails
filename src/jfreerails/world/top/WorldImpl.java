package jfreerails.world.top;

import java.util.ArrayList;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.track.FreerailsTile;

public class WorldImpl implements World {

	private final ArrayList[] lists = new ArrayList[KEY.getNumberOfKeys()];

	private FreerailsTile[][] map;

	public WorldImpl() {
		this.setupMap(0, 0);
		this.setupLists();
	}

	public WorldImpl(int mapWidth, int mapHeight) {
		this.setupMap(mapWidth, mapHeight);
		this.setupLists();
	}

	public void setupMap(int mapWidth, int mapHeight) {
		map = new FreerailsTile[mapWidth][mapHeight];
		for (int x = 0; x < mapWidth; x++) {
			for (int y = 0; y < mapHeight; y++) {
				map[x][y] = FreerailsTile.NULL;
			}
		}
	}

	public void setupLists() {
		for (int i = 0; i < lists.length; i++) {
			lists[i] = new ArrayList();
		}
	}

	public FreerailsSerializable get(KEY key, int index) {
		return (FreerailsSerializable) lists[key.getKeyNumber()].get(index);
	}

	public void set(KEY key, int index, FreerailsSerializable element) {
		lists[key.getKeyNumber()].get(index);
	}

	public void add(KEY key, FreerailsSerializable element) {
		lists[key.getKeyNumber()].add(element);
	}

	public int size(KEY key) {
		return lists[key.getKeyNumber()].size();
	}

	public int getMapWidth() {
		return map.length;
	}

	public int getMapHeight() {					
		if(map.length==0){
			//When the map size is 0*0 we get a java.lang.ArrayIndexOutOfBoundsException: 0
			// if we don't have check above.			 	
			return 0;
		}else{		
			return map[0].length;
		}
	}

	public void setTile(int x, int y, FreerailsTile element) {
		map[x][y] = element;
	}

	public FreerailsTile getTile(int x, int y) {
		return map[x][y];
	}

	public boolean boundsContain(int x, int y) {
		if (x >= 0 && x < map.length && y >= 0 && y < map[0].length) {
			return true;
		} else {
			return false;
		}
	}

	public boolean boundsContain(KEY k, int index) {
		if(index>=0 && index < this.size(k)){
			return true;
		}else{
			return false;
		}
	}

}
