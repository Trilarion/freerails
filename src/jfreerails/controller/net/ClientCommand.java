/*
 * Created on Apr 14, 2004
 */
package jfreerails.controller.net;

import jfreerails.world.common.FreerailsSerializable;


/**
 *
 *  @author Luke
 *
 */
public interface ClientCommand extends FreerailsSerializable {
    CommandStatus execute(ClientControlInterface client);

    int getID();
}