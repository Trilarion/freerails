package jfreerails.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.PriorityQueue;



/** An OpenList for SimpleAStarPathFinder.
 * 
 * @author Luke
 *
 */
class OpenList implements Serializable {
    private static final long serialVersionUID = 3257282539419611442L;
    static class OpenListEntry implements Comparable<OpenList.OpenListEntry>{
    	int f;
    	int node;
    	
    	OpenListEntry(int f, int node){
    		this.f = f;
    		this.node = node;
    	}

		public int compareTo(OpenList.OpenListEntry o) {				
			return f - o.f;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof OpenList.OpenListEntry){
				OpenList.OpenListEntry t = (OpenList.OpenListEntry)obj;
				return t.f == f && t.node == node;
			}
			return false;
		}

		@Override
		public int hashCode() {				
			return f * node;
		}
    	
    }
    
    private final HashMap<Integer, OpenList.OpenListEntry> map= new HashMap<Integer, OpenList.OpenListEntry>();
    private final PriorityQueue<OpenList.OpenListEntry> queue = new PriorityQueue<OpenList.OpenListEntry>();
	
    void clear() {
    	queue.clear();
        map.clear();
    }

    int getF(int node) {
    	return map.get(node).f;            
    }

    void add(int node, int f) {
    	OpenList.OpenListEntry entry = new OpenListEntry(f, node);
    	if(map.containsKey(node)){
    		OpenList.OpenListEntry old = map.get(node);
    		queue.remove(old);
    	}
    	queue.add(entry);
    	map.put(node, entry);            
    }

    boolean contains(int node) {
        return map.containsKey(node);
    }

    int smallestF() {
    	OpenList.OpenListEntry entry = queue.peek();
    	return entry.f;           
    }

    int popNodeWithSmallestF() {
    	OpenListEntry entry = queue.remove();
        int node = entry.node;
        map.remove(node);
        return node;
    }

    int size() {
        return map.size();
    }
    
}