/*
 * Created on 14-Dec-2004
 *
 */
package jfreerails.client.common;

/**
 * @author Luke
 *
 */
public interface ModelRootListener {
	
	void propertyChange(ModelRoot.Property p, Object oldValue, Object newValue);

}
