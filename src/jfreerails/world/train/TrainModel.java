
package jfreerails.world.train;

import jfreerails.world.common.FreerailsSerializable;

public class TrainModel implements FreerailsSerializable{
    
    public static final int MAX_NUMBER_OF_WAGONS=10;
    
    public static final int DISTANCE_BETWEEN_WAGONS=5;
    
    TrainPositionOnMap trainposition;    
    
    int engineType = 0; 
           
    int[] wagonTypes = new int[]{0, 1, 2};
   
    
    public TrainModel(int engine, int[] wagons, TrainPositionOnMap p){
        this.engineType = engine;
        this.wagonTypes=wagons;
        trainposition=p;
    }
    public TrainModel(int engine){
		this.engineType = engine;  
    }
    
    public int getLength(){
        return (1+wagonTypes.length)*32;  //Engine + wagons.
    }
    
    
    public boolean canAddWagon(){
        return wagonTypes.length < MAX_NUMBER_OF_WAGONS;
    }
    
    public int getNumberOfWagons(){
        return wagonTypes.length;
    }
    
    public int getWagon(int i){      
        return wagonTypes[i];        
    }
    
    public void addWagon(int wagonType){
        if(canAddWagon()){
            int oldlength = wagonTypes.length;
            int[] newWagons = new int[oldlength + 1];
            for(int i=0; i<oldlength; i++){
                newWagons[i]=wagonTypes[i];
            }
            newWagons[oldlength] = wagonType;
            wagonTypes = newWagons;
        }else{
            throw new IllegalStateException("Cannot add wagon");
        }
    }
    
    public TrainPositionOnMap getPosition(){
        return  trainposition;
    }
    
    public  void setPosition(TrainPositionOnMap s){
        trainposition=s;
    }
    
    
    
}
