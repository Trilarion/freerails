package jfreerails.server;

import java.awt.Point;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import jfreerails.controller.TrainMover;
import jfreerails.move.ChangeTrainPositionMove;
import jfreerails.util.GameModel;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameTime;
import jfreerails.world.station.ProductionAtEngineShop;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;

/**
 * @author Luke Lindsay 05-Nov-2002
 *
 */
public class ServerGameEngine implements GameModel {

	World world;
	
	TrainBuilder tb;
	
	private int targetTicksPerSecond = 30;
	
	long frameStartTime;	
	long nextModelUpdateDue = System.currentTimeMillis();

	ArrayList trainMovers = new ArrayList();
	
	private int yearCargoLastAddedToStations = -1;

	public int getTargetTicksPerSecond() {
		return targetTicksPerSecond;
	}

	public void setTargetTicksPerSecond(int targetTicksPerSecond) {
		this.targetTicksPerSecond = targetTicksPerSecond;
	}


	public ServerGameEngine(World w) {
		this.world = w;
		setupGame();
	}


	private void setupGame() {
		tb = new TrainBuilder(world, this);
		nextModelUpdateDue = System.currentTimeMillis();
	}

	
	public void update() {
		frameStartTime = System.currentTimeMillis();
		
		if (targetTicksPerSecond > 0) {
			buildTrains();
			while (nextModelUpdateDue < frameStartTime) {
				//update the time first, since other updates might need to know the current time.
				updateGameTime();	
				
				//now do the other updates
				moveTrains();						
				addCargoToStations();						
				nextModelUpdateDue += 1000 / targetTicksPerSecond;
			}
		} else {
			nextModelUpdateDue = frameStartTime;
		}		
	}
	
	private void addCargoToStations(){		
		//Add cargo to stations at the start of each new year.
		GameTime time = (GameTime)world.get(ITEM.TIME);
		GameCalendar calendar = (GameCalendar)world.get(ITEM.CALENDAR);
		int currentYear = calendar.getYear(time.getTime());
		if(this.yearCargoLastAddedToStations != currentYear){
			CargoAtStationsGenerator cargoAtStationsGenerator =
							new CargoAtStationsGenerator();
			cargoAtStationsGenerator.update(world);
			this.yearCargoLastAddedToStations = currentYear;										
		}
	}

	/** Iterator over the stations  
	 * and build trains at any that have their production
	 * field set.
	 *
	 */
	private void buildTrains() {
		for(int i=0; i< world.size(KEY.STATIONS); i++){
			StationModel station = (StationModel)world.get(KEY.STATIONS, i);
			if(null !=station && null != station.getProduction()){
				ProductionAtEngineShop production = station.getProduction();
				Point p = new Point(station.x, station.y);
				tb.buildTrain(production.getEngineType(), production.getWagonTypes(), p);
				station.setProduction(null);	
			}			
		}
	}


	private void moveTrains() {
		
		int deltaDistance = 5;
	
		ChangeTrainPositionMove m = null;
		
		Iterator i = trainMovers.iterator();
		
		while (i.hasNext()) {
			Object o = i.next();
			TrainMover trainMover = (TrainMover) o;
			m = trainMover.update(deltaDistance);
			m.doMove(world);
		}
	}
	
	private void updateGameTime(){
		GameTime gt = (GameTime)world.get(ITEM.TIME);
		int time = gt.getTime();
		time += 1;
		world.set(ITEM.TIME, new GameTime(time));
	}

	public void addTrainMover(TrainMover m) {
		trainMovers.add(m);

	}

	public void saveGame() {

		try {

			System.out.print("Saving game..  ");
			FileOutputStream out = new FileOutputStream("freerails.sav");
			GZIPOutputStream zipout = new GZIPOutputStream(out);

			ObjectOutputStream objectOut = new ObjectOutputStream(zipout);
			objectOut.writeObject(trainMovers);
			objectOut.writeObject(getWorld());
			objectOut.flush();
			objectOut.close();

			System.out.println("done.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void loadGame() {

		try {

			System.out.print("Loading game..  ");
			FileInputStream in = new FileInputStream("freerails.sav");
			GZIPInputStream zipin = new GZIPInputStream(in);
			ObjectInputStream objectIn = new ObjectInputStream(zipin);

			this.trainMovers = (ArrayList) objectIn.readObject();

			this.world = (World) objectIn.readObject();									
			setupGame();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	public void newGame(World w) {

		this.world = w;

		trainMovers = new ArrayList();		
		setupGame();
	}

	/**
	 * Returns the world.
	 * @return World
	 */
	public World getWorld() {
		return world;
	}

}
