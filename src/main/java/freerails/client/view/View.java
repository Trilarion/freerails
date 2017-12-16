/*
 * Created on 23-Mar-2003
 *
 */
package freerails.client.view;

import javax.swing.Action;

import freerails.client.renderer.RenderersRoot;
import freerails.controller.ModelRoot;

/**
 * Defines a standard method to initiate GUI components that need access to the
 * ModelRoot.
 * 
 * @author Luke
 * 
 */
public interface View {
    void setup(ModelRoot modelRoot, RenderersRoot vl, Action closeAction);
}