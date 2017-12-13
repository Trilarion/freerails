/*
 * Created on 23-Mar-2003
 * 
 */
package jfreerails.client.view;

import java.awt.event.ActionListener;

import jfreerails.client.renderer.ViewLists;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * @author Luke
 * 
 */
public interface View {
	
	void setup(ReadOnlyWorld w, ViewLists vl, ActionListener submitButtonCallBack);

}
