package jfreerails.world.train;

import jfreerails.world.common.Money;

final public class EngineType {

	private final String engineTypeName;

	private final int powerAtDrawbar;

	private final Money price;
	
	private boolean available=false;	//Are we allowed to build it?	
        
        private final int maxSpeed;             //speed in mph

	public String getEngineTypeName() {
		return engineTypeName;
	}
	
	public boolean isAvailable(){
		return available;	
	}
	
	public void setAvailable(boolean b){
		this.available=b;	
	}

	public int getPowerAtDrawbar() {
		return powerAtDrawbar;
	}

	public Money getPrice() {
		return price;
	}
        public int getMaxSpeed(){
            return maxSpeed;
        }

	public void getRatedTrainSpeedAtGrade(int speed, int grade) {
	}

	public EngineType(String name, int power, Money m, int speed) {
		engineTypeName = name;
		powerAtDrawbar = power;
		price = m;
                this.maxSpeed=speed;
	}

}