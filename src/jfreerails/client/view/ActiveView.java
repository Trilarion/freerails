/*
 * Created on 15-Dec-2004
 *
 */
package jfreerails.client.view;

import java.awt.event.ActionListener;

import jfreerails.client.renderer.ViewLists;
import jfreerails.controller.ModelRoot;

/**
 * Defines a standard method to initiate GUI components that need access to the
 * ModelRoot <b> and </b> the ActionRoot.
 * 
 * @author Luke
 * 
 */
public interface ActiveView {
	void setup(ModelRoot modelRoot, ActionRoot ar, ViewLists vl,
			ActionListener submitButtonCallBack);
}