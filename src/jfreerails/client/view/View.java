/*
 * Created on 23-Mar-2003
 *
 */
package jfreerails.client.view;

import java.awt.event.ActionListener;

import jfreerails.client.common.ModelRoot;
import jfreerails.client.renderer.ViewLists;


/**
 * Defines a standard method to initiate GUI components that need access to the ModelRoot.
 *
 * @author Luke
 *
 */
public interface View {
    void setup(ModelRoot modelRoot, ViewLists vl,
        ActionListener submitButtonCallBack);
}