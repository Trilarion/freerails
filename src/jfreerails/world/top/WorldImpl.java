package jfreerails.world.top;

import java.util.ArrayList;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.track.FreerailsTile;

public class WorldImpl implements World {
		
	private final ArrayList[] lists = new ArrayList[KEY.getNumberOfKeys()];
	
	private final FreerailsSerializable[] items = new FreerailsSerializable[ITEM.getNumberOfKeys()];

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
		lists[key.getKeyNumber()].set(index, element);
	}

	public int add(KEY key, FreerailsSerializable element) {
		lists[key.getKeyNumber()].add(element);
		return size(key)-1;
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

	public FreerailsSerializable removeLast(KEY key) {
		int size = lists[key.getKeyNumber()].size();
		int index = size - 1;
		return (FreerailsSerializable)lists[key.getKeyNumber()].remove(index);
	}
		
	public boolean equals(Object o) {		
		if(o instanceof WorldImpl){
			WorldImpl test = (WorldImpl)o;
			
			if(lists.length != test.lists.length){
				return false;
			}else{
				for(int i = 0 ; i < lists.length ; i++){
					if(!lists[i].equals(test.lists[i])){
						return false;
					}
				}
			}
			
			if((this.getMapWidth() != test.getMapWidth()) || (this.getMapHeight() != test.getMapHeight())){
				return false;
			}else{
				for(int x = 0 ; x < this.getMapWidth() ; x++){
					for(int y = 0 ; y < this.getMapHeight(); y++){
						if(!getTile(x, y).equals(test.getTile(x, y))){
							System.out.println(getTile(x, y));
							System.out.println(test.getTile(x, y));
							return false;
						}
					}
				}
			}
			if(this.items.length != test.items.length){
				return false;
			}else{
				for(int i = 0; i < this.items.length ; i ++){
					//Some of the elements in the items array might be null, so we check for this before
					//calling equals to avoid NullPointerExceptions.
					if(!(null == items[i] ? null==test.items[i] : items[i].equals(test.items[i]))){
						return false;
					}
				}
			}
			//phew!
			return true;			
		}else{		
			return false;
		}
	}

	public FreerailsSerializable get(ITEM item) {		
		return items[item.getKeyNumber()];
	}

	public void set(ITEM item, FreerailsSerializable element) {
		items[item.getKeyNumber()]=element;		
	}

}
