/*
 * Created on 14-Dec-2004
 *
 */
package freerails.client.common;

import freerails.controller.ModelRoot;

/**
 * @author Luke
 * 
 */
public interface ModelRootListener {

    void propertyChange(ModelRoot.Property p, Object oldValue, Object newValue);

}
