package jfreerails.client.top;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import jfreerails.client.common.ModelRoot;
import jfreerails.client.common.SoundManager;
import jfreerails.client.common.ModelRoot.Property;
import jfreerails.client.renderer.BuildTrackController;
import jfreerails.client.view.ActionRoot;
import jfreerails.client.view.DialogueBoxController;
import jfreerails.client.view.FreerailsCursor;
import jfreerails.client.view.MapViewJComponent;
import jfreerails.client.view.StationBuildModel;
import jfreerails.controller.BuildTrackStrategy;
import jfreerails.controller.TrackMoveProducer;
import jfreerails.move.MoveStatus;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.top.ReadOnlyWorld;
import static jfreerails.controller.TrackMoveProducer.BuildMode.BUILD_TRACK;
import static jfreerails.controller.TrackMoveProducer.BuildMode.IGNORE_TRACK;
import static jfreerails.controller.TrackMoveProducer.BuildMode.BUILD_STATION;


/**
 * Handles key presses and mouse movements on the map - responsible for moving
 * the cursor etc.
 * 
 * @author Luke
 */
public class UserInputOnMapController extends KeyAdapter {

	private class CursorMouseAdapter extends MouseInputAdapter {

		private boolean pressedInside = false;

		public void mouseDragged(MouseEvent evt) {
			
			if( ignoreDragging) return;
			if(!buildTrack()) return;
			
			if (SwingUtilities.isLeftMouseButton(evt) && pressedInside) {

				setIgnoreKeyEvents(true);
				int x = evt.getX();
				int y = evt.getY();

				float scale = mapView.getScale();
				Dimension tileSize = new Dimension((int) scale, (int) scale);
				int tileX = x / tileSize.width;
				int tileY = y / tileSize.height;

				/*
				 * See the javadoc for JComponent.setAutoscrolls(boolean
				 * autoscrolls)
				 */
				assert mapView.getAutoscrolls();

				// Scroll view if necessary.
				if (!mapView.getVisibleRect().contains(x, y)) {
					/*
					 * Making the rectangle we scroll to 2 tiles wide and
					 * centred on x, y means that we scroll at least one tile.
					 * This stops painfully slow scrolling in full screen mode
					 * when the mouse cannot be dragged far from the view port
					 * since it hits the screen edge.
					 */
					Rectangle r = new Rectangle(x - tileSize.width, y
							- tileSize.height, 2 * tileSize.width,
							2 * tileSize.height);
					mapView.scrollRectToVisible(r);
				}

				buildTrack.setProposedTrack(getCursorPosition(), new Point(
						tileX, tileY), trackBuilder);
				mapView.requestFocus();
			}
		}

		public void mousePressed(MouseEvent evt) {
			if (SwingUtilities.isLeftMouseButton(evt)) {
				ignoreDragging = false;
				int x = evt.getX();
				int y = evt.getY();

				float scale = mapView.getScale();
				Dimension tileSize = new Dimension((int) scale, (int) scale);

				// only jump - no track building
				moveCursorJump(new Point(x / tileSize.width, y
						/ tileSize.height));

				mapView.requestFocus();
				pressedInside = true;

				/*
				 * Fix for bug [ 972866 ] Build track by dragging - only when
				 * build track selected
				 */
				boolean isBuildTrackModeSet = trackBuilder
						.getTrackBuilderMode() == BUILD_TRACK;

				if (isBuildTrackModeSet) {
					buildTrack.show();
				}
			} else if (SwingUtilities.isRightMouseButton(evt)) {
				// Cancel building track.
				buildTrack.hide();
				ignoreDragging = true;
				setIgnoreKeyEvents(false);

			}
		}

		public void mouseReleased(MouseEvent evt) {

			if (SwingUtilities.isLeftMouseButton(evt)) {
				ignoreDragging = false;
				setIgnoreKeyEvents(false);
				// build a railroad from x,y to current cursor position
				if (pressedInside && buildTrack.isBuilding()
						&& buildTrack.isBuildTrackSuccessful()) {
					// Fix for bug [ 997088 ]
					// Is current position different from original position?
					int x = evt.getX();
					int y = evt.getY();
					float scale = mapView.getScale();
					Dimension tileSize = new Dimension((int) scale, (int) scale);
					int tileX = x / tileSize.width;
					int tileY = y / tileSize.height;

					if (getCursorPosition().getX() != tileX
							|| getCursorPosition().getY() != tileY) {
						// copy WorldDifferences from buildTrack to World
						Point newPosition = buildTrack
								.updateWorld(trackBuilder);
						setCursorPosition(newPosition);
					}
				}

				pressedInside = false;
				buildTrack.hide();
			}
		}
	}
	private static final String JFREERAILS_CLIENT_SOUNDS_BUILDTRACK_WAV = "/jfreerails/client/sounds/buildtrack.wav";

	private static final Logger logger = Logger
			.getLogger(UserInputOnMapController.class.getName());

	private final ActionRoot actionRoot;

	private BuildIndustryJPopupMenu buildIndustryJPopupMenu = new BuildIndustryJPopupMenu();

	private BuildTrackController buildTrack;

	private DialogueBoxController dialogueBoxController;

	private boolean ignoreDragging = false;

	private MapViewJComponent mapView;

	private final ModelRoot modelRoot;

	private final MouseInputAdapter mouseInputAdapter = new CursorMouseAdapter();

	private SoundManager soundManager = SoundManager.getSoundManager();

	private StationTypesPopup stationTypesPopup;

	private TrackMoveProducer trackBuilder;

	public UserInputOnMapController(ModelRoot mr, ActionRoot ar) {
		modelRoot = mr;
		actionRoot = ar;
	}
	
	private boolean buildTrack(){
		/*
		 * Fix for bug [ 972866 ] Build track by dragging - only when build
		 * track selected
		 */
		TrackMoveProducer.BuildMode trackBuilderMode = trackBuilder.getTrackBuilderMode();
		if (trackBuilderMode == IGNORE_TRACK) return false;
		if (trackBuilderMode == BUILD_STATION) return false;			
		Object cursorMode = modelRoot.getProperty(ModelRoot.Property.CURSOR_MODE);
		if (!ModelRoot.Value.BUILD_TRACK_CURSOR_MODE.equals(cursorMode)) return false;
		return true;
	}

	private void cancelProposedBuild() {
		ignoreDragging = true;
		buildTrack.hide();
		StationBuildModel sbm = actionRoot.getStationBuildModel();
		sbm.getStationCancelAction().actionPerformed(
				new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
		setIgnoreKeyEvents(false);
	}

	private void cursorJumped(Point to) {
		// if (trackBuilder.getTrackBuilderMode() ==
		// TrackMoveProducer.UPGRADE_TRACK) {
		// MoveStatus ms = trackBuilder.upgradeTrack(to);
		//
		// if (ms.ok) {
		// setCursorMessage("");
		// playAppropriateSound();
		// } else {
		// setCursorMessage(ms.message);
		// }
		// }
	}

	private void cursorOneTileMove(Point oldPosition, OneTileMoveVector vector) {		
		
		if(null == trackBuilder){
			logger.warning("No track builder available!");
			return;
		}
		
		if (buildTrack()) {
			trackBuilder.setBuildTrackStrategy(getBts());
			MoveStatus ms = trackBuilder.buildTrack(oldPosition, vector);

			if (ms.ok) {
				setCursorMessage("");
				playAppropriateSound();
			} else {
				setCursorMessage(ms.message);
			}
		} 
	}

	private BuildTrackStrategy getBts() {
		BuildTrackStrategy bts = (BuildTrackStrategy) modelRoot
				.getProperty(ModelRoot.Property.BUILD_TRACK_STRATEGY);
		if (null == bts)
			throw new NullPointerException();
		return bts;
	}

	private Point getCursorPosition() {
		Point point = (Point) modelRoot
				.getProperty(ModelRoot.Property.CURSOR_POSITION);

		// Check for null & make a defensive copy
		point = null == point ? new Point() : new Point(point);

		return point;
	}

	private boolean isIgnoreKeyEvents() {
		Boolean b = (Boolean) modelRoot.getProperty(Property.IGNORE_KEY_EVENTS);
		return b.booleanValue();
	}

	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (isIgnoreKeyEvents()) {
			if (keyCode == KeyEvent.VK_ESCAPE) {
				setIgnoreKeyEvents(false);
			} else {
				return;
			}
		}

		Point cursorPosition = getCursorPosition();

		switch (keyCode) {
		case KeyEvent.VK_NUMPAD1:
			moveCursorOneTile(OneTileMoveVector.SOUTH_WEST);

			break;

		case KeyEvent.VK_NUMPAD2:
			moveCursorOneTile(OneTileMoveVector.SOUTH);

			break;

		case KeyEvent.VK_NUMPAD3:
			moveCursorOneTile(OneTileMoveVector.SOUTH_EAST);

			break;

		case KeyEvent.VK_NUMPAD4:
			moveCursorOneTile(OneTileMoveVector.WEST);

			break;

		case KeyEvent.VK_NUMPAD6:
			moveCursorOneTile(OneTileMoveVector.EAST);

			break;

		case KeyEvent.VK_NUMPAD7:
			moveCursorOneTile(OneTileMoveVector.NORTH_WEST);

			break;

		case KeyEvent.VK_NUMPAD8:
			moveCursorOneTile(OneTileMoveVector.NORTH);

			break;

		case KeyEvent.VK_NUMPAD9:
			moveCursorOneTile(OneTileMoveVector.NORTH_EAST);

			break;

		case KeyEvent.VK_F8: {
			// Check whether we can built a station here before proceeding.
			if (stationTypesPopup.canBuiltStationHere(cursorPosition)) {
				float scale = mapView.getScale();
				Dimension tileSize = new Dimension((int) scale, (int) scale);
				int x = cursorPosition.x * tileSize.width;
				int y = cursorPosition.y * tileSize.height;
				stationTypesPopup.showMenu(mapView, x, y, cursorPosition);
			} else {
				modelRoot.setProperty(Property.QUICK_MESSAGE, "Can't"
						+ " build station here!");
			}

			break;
		}

		case KeyEvent.VK_BACK_SPACE:
			logger.info("Undo building track currently not implemented.");

			//
			// MoveStatus ms = trackBuilder.undoLastTrackMove();
			//
			// if (!ms.isOk()) {
			// setCursorMessage(ms.message);
			// }
			break;

		case KeyEvent.VK_I: {
			dialogueBoxController.showStationOrTerrainInfo(cursorPosition.x,
					cursorPosition.y);

			break;
		}

		case KeyEvent.VK_C: {
			mapView.centerOnTile(cursorPosition);

			break;
		}

		case KeyEvent.VK_B: {
			float scale = mapView.getScale();
			Dimension tileSize = new Dimension((int) scale, (int) scale);
			int x = cursorPosition.x * tileSize.width;
			int y = cursorPosition.y * tileSize.height;
			buildIndustryJPopupMenu.setCusorLocation(cursorPosition);
			buildIndustryJPopupMenu.show(mapView, x, y);

			break;
		}
		case KeyEvent.VK_ESCAPE: {
			cancelProposedBuild();

			break;
		}
		case KeyEvent.VK_F12: {
			System.out.println("Disable keyboard input!");
			setIgnoreKeyEvents(true);
			break;
		}
		}
	}

	/**
	 * Checks whether specified point is in legal rectangle.
	 * 
	 * @param tryThisPoint
	 *            Point
	 * @return boolean
	 */
	private boolean legalRectangleContains(Point tryThisPoint) {
		ReadOnlyWorld world = modelRoot.getWorld();
		int width = world.getMapWidth();
		int height = world.getMapHeight();
		Rectangle legalRectangle = new Rectangle(0, 0, width, height);

		return legalRectangle.contains(tryThisPoint);
	}

	private void moveCursorJump(Point tryThisPoint) {
		setCursorMessage("");

		if (legalRectangleContains(tryThisPoint)) {
			setCursorPosition(tryThisPoint);
			cursorJumped(tryThisPoint);
		} else {
			this.setCursorMessage("Illegal cursor position!");
		}
	}

	private void moveCursorOneTile(OneTileMoveVector v) {
		setCursorMessage(null);

		Point cursorMapPosition = this.getCursorPosition();
		Point tryThisPoint = new Point(cursorMapPosition.x + v.getDx(),
				cursorMapPosition.y + v.getDy());

		/* Move the cursor. */
		if (legalRectangleContains(tryThisPoint)) {
			setCursorPosition(tryThisPoint);
			cursorOneTileMove(cursorMapPosition, v);
		} else {
			this.setCursorMessage("Illegal cursor position!");
		}
	}

	private void playAppropriateSound() {
		switch (trackBuilder.getTrackBuilderMode()) {
		case BUILD_TRACK:
		case UPGRADE_TRACK:
			soundManager.playSound(JFREERAILS_CLIENT_SOUNDS_BUILDTRACK_WAV, 0);
			break;
		case REMOVE_TRACK:
			soundManager.playSound("/jfreerails/client/sounds/removetrack.wav",
					0);
			break;
		default:
		// do nothing
		}
	}

	private void setCursorMessage(String s) {
		modelRoot.setProperty(Property.CURSOR_MESSAGE, s);
	}

	private void setCursorPosition(Point p) {
		// Make a defensive copy.
		Point point = new Point(p);
		modelRoot.setProperty(Property.CURSOR_POSITION, point);
	}

	private void setIgnoreKeyEvents(boolean ignoreKeyEvents) {
		modelRoot.setProperty(Property.IGNORE_KEY_EVENTS, Boolean
				.valueOf(ignoreKeyEvents));
	}

	public void setup(MapViewJComponent mv, TrackMoveProducer trackBuilder,
			StationTypesPopup stPopup, ModelRoot mr, DialogueBoxController dbc,
			FreerailsCursor cursor, BuildTrackController buildTrack) {
		this.dialogueBoxController = dbc;
		this.mapView = mv;
		this.stationTypesPopup = stPopup;
		this.trackBuilder = trackBuilder;
		this.buildTrack = buildTrack;
		buildIndustryJPopupMenu.setup(mr, null, null);

		/*
		 * We attempt to remove listeners before adding them to prevent them
		 * being added several times.
		 */
		mapView.removeMouseListener(mouseInputAdapter);
		mapView.addMouseListener(mouseInputAdapter);
		mapView.removeMouseMotionListener(mouseInputAdapter);
		mapView.addMouseMotionListener(mouseInputAdapter);
		mapView.removeKeyListener(this);
		mapView.addKeyListener(this);
	}
}