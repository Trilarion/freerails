/*
 * Created on 15-Dec-2004
 *
 */
package freerails.client.view;

import freerails.client.renderer.RenderersRoot;
import freerails.controller.ModelRoot;

import java.awt.event.ActionListener;

/**
 * Defines a standard method to initiate GUI components that need access to the
 * ModelRoot <b> and </b> the ActionRoot.
 *
 * @author Luke
 */
public interface ActiveView {

    /**
     *
     * @param modelRoot
     * @param ar
     * @param vl
     * @param submitButtonCallBack
     */
    void setup(ModelRoot modelRoot, ActionRoot ar, RenderersRoot vl,
               ActionListener submitButtonCallBack);
}