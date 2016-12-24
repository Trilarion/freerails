/*
 * Created on 23-Mar-2003
 *
 */
package jfreerails.client.view;

import javax.swing.Action;

import jfreerails.client.renderer.RenderersRoot;
import jfreerails.controller.ModelRoot;

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