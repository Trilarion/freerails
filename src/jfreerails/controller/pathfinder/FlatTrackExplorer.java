package jfreerails.controller.pathfinder;

import java.awt.Point;
import java.util.NoSuchElementException;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackPiece;


public class FlatTrackExplorer implements Explorer, FreerailsSerializable {

	PositionOnTrack currentPosition = new PositionOnTrack(0, 0, OneTileMoveVector.NORTH);
	PositionOnTrack currentBranch = new PositionOnTrack(0, 0, OneTileMoveVector.NORTH);

	boolean beforeFirst = true;

	

	private World w;

	private int currentStation; //stores the index number of a station if a train is currently at a station

	public World getWorld() {
		return w;
	}

	
	public void setPosition(int i) {
		beforeFirst = true;
		currentPosition.setValuesFromInt(i);
	}

	public int getPosition() {
		return this.currentPosition.toInt();
	}

	public void moveForward() {
		this.setPosition(this.getBranchPosition());
	}

	public void nextBranch() {

		if (!hasNextBranch()) {
			throw new NoSuchElementException();
		} else {
			OneTileMoveVector v = this.getFirstVectorToTry();
			OneTileMoveVector lastToTry = this.currentPosition.getDirection().getOpposite();

			Point p = new Point(currentPosition.getX(), currentPosition.getY());
			TrackPiece tp = (TrackPiece)w.getTile(p.x, p.y);
			TrackConfiguration conf = tp.getTrackConfiguration();
			OneTileMoveVector[] vectors = OneTileMoveVector.getList();

			int i = v.getNumber();

			int loopCounter = 0;
			while (!conf.contains(vectors[i].getTemplate())) {
				i++;
				i = i % 8;
				loopCounter++;
				if (8 < loopCounter) {
					throw new IllegalStateException();
					//This should never happen.
				}
			}

			OneTileMoveVector branchDirection = OneTileMoveVector.getInstance(i);
			this.currentBranch.setDirection(branchDirection);
			int x = this.currentPosition.getX() + branchDirection.deltaX;
			int y = this.currentPosition.getY() + branchDirection.deltaY;
			
			/*****************************************************************/
			boolean trainOnStation = isAtStation();
			if (isAtStation()) {
				System.out.println("train is at station #" + this.currentStation);
			}
			/*****************************************************************/
			
			this.currentBranch.setX(x);
			this.currentBranch.setY(y);
		}
		beforeFirst = false;
	}

	
	public int getBranchPosition() {
		return currentBranch.toInt();
	}

	
	public int getBranchLength() {
		return currentBranch.getDirection().getLength();
	}

	
	public boolean hasNextBranch() {

		if (beforeFirst) {
			//We can always go back the way we have come, so if we are before the first
			//branch, there must be a branch: the one we used to get here.

			return true;
		} else {

			//Since we can always go back the way we have come, if the direction of 
			//current branch is not equal to the opposite of the current direction,
			//there must be another branch.
			OneTileMoveVector currentBranchDirection = this.currentBranch.getDirection();
			OneTileMoveVector oppositeToCurrentDirection = this.currentPosition.getDirection().getOpposite();

			if (oppositeToCurrentDirection.getNumber() == currentBranchDirection.getNumber()) {
				return false;
			} else {
				return true;
			}
		}
	}

	public FlatTrackExplorer(World world, PositionOnTrack p) {
		w = world;
		this.currentPosition = new PositionOnTrack(p.getX(), p.getY(), p.getDirection());
	}

	/******************************************************************************************/
	//scott bennett 15/03/03
	public FlatTrackExplorer(PositionOnTrack p, World world) {
		this.currentPosition = new PositionOnTrack(p.getX(), p.getY(), p.getDirection());
		this.w = world;
	}

	public boolean isAtStation() {
		
		//loop thru the station list to check if train is at the same Point as a station
		for (int i=0; i<w.size(KEY.STATIONS); i++) {
			StationModel tempPoint = (StationModel)w.get(KEY.STATIONS, i);
			if ( (this.currentPosition.getX() == tempPoint.x ) && (this.currentPosition.getY() == tempPoint.y ) ) {
				this.currentStation = i;
				return true; //train is at this station at location tempPoint
			}
		}

		return false; 	//there are no stations that exist where the train is currently
	}
	/******************************************************************************************/
	

	public static PositionOnTrack[] getPossiblePositions(World w, Point p) {
		TrackPiece tp = (TrackPiece)w.getTile(p.x, p.y);
		TrackConfiguration conf = tp.getTrackConfiguration();
		OneTileMoveVector[] vectors = OneTileMoveVector.getList();

		//Count the number of possible positions.
		int n = 0;
		for (int i = 0; i < vectors.length; i++) {
			if (conf.contains(vectors[i].getTemplate())) {
				n++;
			}
		}

		PositionOnTrack[] possiblePositions = new PositionOnTrack[n];

		n = 0;
		for (int i = 0; i < vectors.length; i++) {
			if (conf.contains(vectors[i].getTemplate())) {

				possiblePositions[n] = new PositionOnTrack(p.x, p.y, vectors[i].getOpposite());
				n++;
			}
		}
		return possiblePositions;
	}

	OneTileMoveVector getFirstVectorToTry() {
		if (beforeFirst) {

			//Return the vector that is 45 degrees clockwise from the oppposite 
			//of the current position.
			OneTileMoveVector v = this.currentPosition.getDirection();
			v = v.getOpposite();
			int i = v.getNumber();
			i++;
			i = i % 8;
			v = OneTileMoveVector.getInstance(i);
			return v;
		} else {

			//Return the vector that is 45 degrees clockwise from the direction  
			//of the current branch.
			OneTileMoveVector v = this.currentBranch.getDirection();
			int i = v.getNumber();
			i++;
			i = i % 8;
			v = OneTileMoveVector.getInstance(i);
			return v;
		}
	}

}
