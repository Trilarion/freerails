package jfreerails.world.train;

import java.util.Arrays;

import jfreerails.world.common.FreerailsSerializable;

public class TrainModel implements FreerailsSerializable {

	public static final int MAX_NUMBER_OF_WAGONS = 10;

	public static final int DISTANCE_BETWEEN_WAGONS = 5;

	private int scheduleID;

	TrainPositionOnMap trainposition;

	int engineType = 0;

	final int[] wagonTypes;

	private int cargoBundleNumber;
	
	public  TrainModel getNewInstance(int newEngine, int[] newWagons){
		return new TrainModel(newEngine, newWagons, this.getPosition(),  this.getScheduleID(), this.getCargoBundleNumber());		
	}
	public TrainModel(
		int engine,
		int[] wagons,
		TrainPositionOnMap p,
		int scheduleID,
		int BundleId) { //World world){
		this.engineType = engine;
		this.wagonTypes = wagons;
		trainposition = p;
		this.scheduleID = scheduleID;
		this.cargoBundleNumber = BundleId;
	}

	public TrainModel(int[] wagons, int BundleId) {
		this.wagonTypes = wagons;
		this.cargoBundleNumber = BundleId;
	}

	public TrainModel(
		int engine,
		int[] wagons,
		TrainPositionOnMap p,
		int scheduleID) {
		this.engineType = engine;
		this.wagonTypes = wagons;
		trainposition = p;
		this.scheduleID = scheduleID;
	}

	public TrainModel(int engine) {
		this.engineType = engine;
		wagonTypes = new int[] { 0, 1, 2 };
	}

	public int getLength() {
		return (1 + wagonTypes.length) * 32; //Engine + wagons.
	}

	public boolean canAddWagon() {
		return wagonTypes.length < MAX_NUMBER_OF_WAGONS;
	}

	public int getNumberOfWagons() {
		return wagonTypes.length;
	}

	public int getWagon(int i) {
		return wagonTypes[i];
	}

	

//	public void addWagon(int wagonType) {
//		if (canAddWagon()) {
//			int oldlength = wagonTypes.length;
//			int[] newWagons = new int[oldlength + 1];
//			for (int i = 0; i < oldlength; i++) {
//				newWagons[i] = wagonTypes[i];
//			}
//			newWagons[oldlength] = wagonType;
//			wagonTypes = newWagons;
//		} else {
//			throw new IllegalStateException("Cannot add wagon");
//		}
//	}

	public TrainPositionOnMap getPosition() {
		return trainposition;
	}

	public void setPosition(TrainPositionOnMap s) {
		trainposition = s;
	}

	public int getEngineType() {
		return engineType;
	}

	public int getCargoBundleNumber() {
		return cargoBundleNumber;
	}

	public int getScheduleID() {
		return scheduleID;
	}

	public boolean equals(Object obj) {
		if( obj instanceof TrainModel){
			TrainModel test = (TrainModel)obj;
			boolean b = this.cargoBundleNumber == test.cargoBundleNumber
			&& this.engineType == test.engineType
			&& null == this.trainposition ? null == test.trainposition : this.trainposition.equals(test.trainposition)
			&& Arrays.equals(this.wagonTypes, test.wagonTypes)
			&& this.scheduleID == test.scheduleID;
			return b;
		}else{
			return false;
		}
	}

}
