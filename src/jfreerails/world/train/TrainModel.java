
package jfreerails.world.train;

import java.util.NoSuchElementException;

import jfreerails.world.misc.FreerailsSerializable;

public class TrainModel implements FreerailsSerializable{
	
	public static final int MAX_NUMBER_OF_WAGONS=10;
	
	public static final int DISTANCE_BETWEEN_WAGONS=5;	
    
    TrainPosition trainposition;
    
    EngineModel engine;
    
    WagonModel[] wagons = new  WagonModel[MAX_NUMBER_OF_WAGONS];
    
    int numberOfWagons = 10;
    
    
    
    public TrainModel(EngineModel e, TrainPosition p){
        engine=e;
        trainposition=p;
    }
    public TrainModel(EngineModel e){
        engine=e;
        
    }
    
    public int getLength(){
    	return (1+numberOfWagons)*32;  //Engine + wagons.
    }
    	
    
    public boolean canAddWagon(){
    	return numberOfWagons < MAX_NUMBER_OF_WAGONS;
    }
    
    public int getNumberOfWagons(){
        return numberOfWagons;
    }
    
    public WagonModel getWagon(int i){
    	if(i<=numberOfWagons){
    		return wagons[i];
    	}else{
    		throw new NoSuchElementException();	
    	}	
    }
    
    public void addWagon(WagonModel w){
    	if(canAddWagon()){
    		wagons[numberOfWagons]=w;
    		numberOfWagons++;		
    	}else{
    		throw new IllegalStateException("Cannot add wagon");
    	}    		    			
    }
    
    public TrainPosition getPosition(){
        return  trainposition;
    }
    
    public  void setPosition(TrainPosition s){
          trainposition=s;
    }
    
    public EngineModel getEngine(){
    	return engine;    	
    }
    
    public void setEngine( EngineModel e){
    	this.engine=e;
    }
    	
}
