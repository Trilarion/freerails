/*
 * Created on 18-Feb-2005
 *
 */
package jfreerails.controller;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import jfreerails.move.AddItemToListMove;
import jfreerails.move.AddTransactionMove;
import jfreerails.move.CompositeMove;
import jfreerails.move.Move;
import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.cargo.ImmutableCargoBundle;
import jfreerails.world.common.Money;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.train.EngineType;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.PathOnTiles;
import jfreerails.world.train.SpeedAgainstTime;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainMotion;
import jfreerails.world.train.TrainStatus;

/**
 * @author Luke
 *
 */
public class AddTrainPreMove implements PreMove{
	
	private static final long serialVersionUID = 4050201951105069624L;
	private final int engineTypeId;
	private final int[] wagons;
	private final Point point;
	private final FreerailsPrincipal principal;
	private final ImmutableSchedule schedule;
	
	
    public AddTrainPreMove(int e, int[] wags, Point p,
        FreerailsPrincipal fp, ImmutableSchedule s) { 
    	engineTypeId = e;
    	wagons = wags;
    	point = p;
    	principal = fp;
    	schedule = s;
    }     
    
    PathOnTiles initPositionStep1(ReadOnlyWorld w){
    	PositionOnTrack[] pp = FlatTrackExplorer.getPossiblePositions(w, point);
    	FlatTrackExplorer fte = new FlatTrackExplorer(w, pp[0]);
    		    	
    	List<OneTileMoveVector> vectors = new ArrayList<OneTileMoveVector>();
    	int length = calTrainLength();
    	int distanceTravelled = 0;
    	PositionOnTrack p = new PositionOnTrack();
    	while(distanceTravelled < length){
    		fte.nextEdge();
    		fte.moveForward();
    		p.setValuesFromInt(fte.getPosition());
    		OneTileMoveVector v = p.cameFrom();
    		distanceTravelled += v.getLength();
    		vectors.add(v);
    		
    	}
    	return new PathOnTiles(point, vectors);
    }

	private int calTrainLength() {
		TrainModel train = new TrainModel(engineTypeId, wagons, 0);
		int length = train.getLength();
		return length;
	}
    
    TrainMotion initPositionStep2(PathOnTiles path){
    	TrainMotion tm = new TrainMotion(path, calTrainLength(), SpeedAgainstTime.STOPPED);
    	return tm;
    }

	/* (non-Javadoc)
	 * @see jfreerails.controller.PreMove#generateMove(jfreerails.world.top.ReadOnlyWorld)
	 */
	public Move generateMove(ReadOnlyWorld w) {
		//Add cargo bundle.
		int bundleId = w.size(KEY.CARGO_BUNDLES, principal);
		ImmutableCargoBundle cargo = ImmutableCargoBundle.EMPTY_BUNDLE;
		AddItemToListMove addCargoBundle = new AddItemToListMove(KEY.CARGO_BUNDLES, bundleId, cargo, principal);
		
		//Add schedule
		int scheduleId = w.size(KEY.TRAIN_SCHEDULES, principal);
		AddItemToListMove addSchedule = new AddItemToListMove(KEY.TRAIN_SCHEDULES, scheduleId, schedule, principal);
				
		//Add train to train list.
		TrainModel train = new TrainModel(engineTypeId, wagons, scheduleId, bundleId);
		int trainId =  w.size(KEY.TRAINS, principal);
		AddItemToListMove addTrain  = new AddItemToListMove(KEY.TRAINS, trainId, train, principal);
								
		//Pay for train.		
		int quantity = 1;
		/* Determine the price of the train.*/
        EngineType engineType = (EngineType)w.get(SKEY.ENGINE_TYPES,
                engineTypeId);
        Money price = engineType.getPrice();
        Transaction transaction = new AddItemTransaction(Transaction.Category.TRAIN,
        		engineTypeId, quantity, new Money(-price.getAmount()));
        AddTransactionMove transactionMove = new AddTransactionMove(principal,
                transaction);
		
		//Setup and add train position.
        
        PathOnTiles path = initPositionStep1(w);
        TrainMotion motion = initPositionStep2(path);
        
        int motionId1 = w.size(KEY.TRAIN_MOTION1, principal);
        AddItemToListMove addPosition1 = new AddItemToListMove(KEY.TRAIN_MOTION1, motionId1, motion, principal);
		
        int motionId2 = w.size(KEY.TRAIN_MOTION2, principal);
        AddItemToListMove addPosition2 = new AddItemToListMove(KEY.TRAIN_MOTION2, motionId2, motion, principal);
        
        int statusId = w.size(KEY.TRAIN_STATUS, principal);
        TrainStatus status = new TrainStatus(TrainStatus.Status.STOPPED_AT_SIGNAL);
        AddItemToListMove addStatus = new AddItemToListMove(KEY.TRAIN_STATUS, statusId, status, principal);
        
        Move[] moves = {addCargoBundle, addSchedule, addTrain, transactionMove, addPosition1, addPosition2, addStatus};
        
		return new CompositeMove(moves);
	}
    
    
        

}
