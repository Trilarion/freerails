/*
 * RunMe.java
 *
 * Created on 23 June 2002, 02:44
 */

package experimental;
import javax.swing.JFrame;

import jfreerails.client.top.GameLoop;
import jfreerails.client.common.ScreenHandler;
/**
 *
 * @author  Luke Lindsay
 */
public class RunMe {

	/** Creates new RunMe */
	public RunMe() {
	}

	/**
	* @param args the command line arguments
	*/
	public static void main(String args[]) {
		JFrame jFrame =
			new jfreerails.client.top.ClientJFrame(
				new SimpleComponentFactoryImpl2());
		//jFrame.show();
		ScreenHandler screenHandler =
			new ScreenHandler(jFrame, ScreenHandler.WINDOWED_MODE, null);
		GameLoop gameLoop = new GameLoop(screenHandler);
		Thread t = new Thread(gameLoop);
		t.start();
	}

}
