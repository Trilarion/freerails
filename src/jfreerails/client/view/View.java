/*
 * Created on 23-Mar-2003
 * 
 */
package jfreerails.client.view;

import java.awt.event.ActionListener;

/**
 * @author Luke
 * 
 */
public interface View {
	
	void setup(ModelRoot modelRoot, ActionListener submitButtonCallBack);

}
