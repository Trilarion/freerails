/*
 * Created on 14-Dec-2004
 *
 */
package jfreerails.client.common;

import jfreerails.controller.ModelRoot;

/**
 * @author Luke
 * 
 */
public interface ModelRootListener {

    void propertyChange(ModelRoot.Property p, Object oldValue, Object newValue);

}
