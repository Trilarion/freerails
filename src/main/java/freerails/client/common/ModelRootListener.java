/*
 * Created on 14-Dec-2004
 *
 */
package freerails.client.common;

import freerails.controller.ModelRoot;

/**
 * @author Luke
 */
public interface ModelRootListener {

    /**
     *
     * @param p
     * @param oldValue
     * @param newValue
     */
    void propertyChange(ModelRoot.Property p, Object oldValue, Object newValue);

}
