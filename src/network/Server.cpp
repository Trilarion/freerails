/*
 * $Id$
 */

#include "Server.h"
#include <iostream.h>

Server::Server(int port):TCPConnection() {

  listen(port);
  
}

Server::~Server() {

}
