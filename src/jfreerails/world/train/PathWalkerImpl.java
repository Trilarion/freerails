package jfreerails.world.train;

public class PathWalkerImpl implements PathWalker {
    
    FreerailsPathIterator it;
    
    public PathWalkerImpl(FreerailsPathIterator i){
        
        it=i;
        
    }

	public boolean canStepForward() {
		return false;
	}

	

	public FreerailsPathIterator stepForward() {
		return null;
	}

	public FreerailsPathIterator stepForward(int distance) {
		return null;
	}

}
