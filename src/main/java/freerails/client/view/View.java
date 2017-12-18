/*
 * Created on 23-Mar-2003
 *
 */
package freerails.client.view;

import freerails.client.renderer.RenderersRoot;
import freerails.controller.ModelRoot;

import javax.swing.*;

/**
 * Defines a standard method to initiate GUI components that need access to the
 * ModelRoot.
 *
 * @author Luke
 */
public interface View {

    /**
     *
     * @param modelRoot
     * @param vl
     * @param closeAction
     */
    void setup(ModelRoot modelRoot, RenderersRoot vl, Action closeAction);
}