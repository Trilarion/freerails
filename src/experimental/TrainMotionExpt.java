package experimental;

import static jfreerails.world.common.OneTileMoveVector.EAST;
import static jfreerails.world.common.OneTileMoveVector.NORTH;
import static jfreerails.world.common.OneTileMoveVector.NORTH_EAST;
import static jfreerails.world.common.OneTileMoveVector.NORTH_WEST;
import static jfreerails.world.common.OneTileMoveVector.SOUTH;
import static jfreerails.world.common.OneTileMoveVector.SOUTH_EAST;
import static jfreerails.world.common.OneTileMoveVector.SOUTH_WEST;
import static jfreerails.world.common.OneTileMoveVector.WEST;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import jfreerails.client.common.ScreenHandler;
import jfreerails.client.top.GameLoop;
import jfreerails.controller.AddTrainPreMove;
import jfreerails.controller.MoveExecutor;
import jfreerails.controller.SimpleMoveExecutor;
import jfreerails.controller.TrackMoveProducer;
import jfreerails.controller.TrainAccessor;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.server.MapFixtureFactory2;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.IntLine;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.World;
import jfreerails.world.train.PathOnTiles;
import jfreerails.world.train.TrainMotion;
import jfreerails.world.train.TrainPositionOnMap;

/**
 * This class is a visual test for the train movement code.
 * 
 * TODO (1) Start train moving on first track section. Use modulus operator on t
 * value. (2) Update the train's motion when the current time inteval is coming
 * to an end. see MoveTrainPreMove.
 * 
 * @author Luke Lindsay
 * 
 */
public class TrainMotionExpt extends JComponent {

	private static final long serialVersionUID = 3690191057862473264L;

	private final World world;

	private final FreerailsPrincipal principal;

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		long l = System.currentTimeMillis();
		l = (l / 1000) % 3;
		TrainAccessor ta = new TrainAccessor(world, principal, 0);

		GameTime gameTime = new GameTime(0);
		TrainMotion motion = ta.findCurrentMotion(gameTime);
		TrainPositionOnMap pos = motion.getPosition(gameTime);

		PathOnTiles pathOT = motion.getTiles(new GameTime(0));
		Iterator<Point> it = pathOT.tiles();
		while (it.hasNext()) {
			Point tile = it.next();
			int x = tile.x * OneTileMoveVector.TILE_DIAMETER;
			int y = tile.y * OneTileMoveVector.TILE_DIAMETER;
			int w = OneTileMoveVector.TILE_DIAMETER;
			int h = OneTileMoveVector.TILE_DIAMETER;
			g.setColor(Color.WHITE);
			g.fillRect(x, y, w, h);
			g.setColor(Color.LIGHT_GRAY);
			g.drawRect(x, y, w, h);
		}
		g.setColor(Color.BLACK);
		IntLine line = new IntLine();
		FreerailsPathIterator path = pos.path();
		while (path.hasNext()) {
			path.nextSegment(line);
			g.drawLine(line.x1, line.y1, line.x2, line.y2);
		}

	}

	public TrainMotionExpt() {
		world = MapFixtureFactory2.getCopy();
		MoveExecutor me = new SimpleMoveExecutor(world, 0);
		principal = me.getPrincipal();
		TrackMoveProducer producer = new TrackMoveProducer(me, world);
		OneTileMoveVector[] trackPath = { EAST, SOUTH_EAST, SOUTH, SOUTH_WEST,
				WEST, NORTH_WEST, NORTH, NORTH_EAST };
		Point from = new Point(5, 5);
		MoveStatus ms = producer.buildTrack(from, trackPath);
		if (!ms.ok)
			throw new IllegalStateException(ms.message);
		AddTrainPreMove addTrain = new AddTrainPreMove(0, new int[0], from,
				principal, null);
		Move m = addTrain.generateMove(world);
		ms = m.doMove(world, principal);
		if (!ms.ok)
			throw new IllegalStateException(ms.message);

	}

	public static void main(String[] args) {
		System.setProperty("SHOWFPS", "true");

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.getContentPane().add(new TrainMotionExpt());

		ScreenHandler screenHandler = new ScreenHandler(f,
				ScreenHandler.WINDOWED_MODE);
		screenHandler.apply();

		GameLoop gameLoop = new GameLoop(screenHandler);
		Thread t = new Thread(gameLoop);
		t.start();
	}
}