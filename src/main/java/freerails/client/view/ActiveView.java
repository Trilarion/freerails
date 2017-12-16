/*
 * Created on 15-Dec-2004
 *
 */
package freerails.client.view;

import java.awt.event.ActionListener;

import freerails.client.renderer.RenderersRoot;
import freerails.controller.ModelRoot;

/**
 * Defines a standard method to initiate GUI components that need access to the
 * ModelRoot <b> and </b> the ActionRoot.
 * 
 * @author Luke
 * 
 */
public interface ActiveView {
    void setup(ModelRoot modelRoot, ActionRoot ar, RenderersRoot vl,
            ActionListener submitButtonCallBack);
}