
package jfreerails.client.common;
import java.awt.AWTEvent;
import java.awt.EventQueue;

/** This event queue is  synchronized on an object passed to its constructor.  This lets one control 
 * when events can be dispatched.
 * @author Luke
 *
 */

final public class SynchronizedEventQueue extends EventQueue {
   
   	private final Object mutex;
   	
   	public SynchronizedEventQueue(Object object){
   		this.mutex = object;
   	}
   
    protected void dispatchEvent(AWTEvent event) {
        synchronized (mutex) {
			super.dispatchEvent(event);
        }
    }
	
	public Object getMutex() {
		return mutex;
	}

}