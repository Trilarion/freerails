/*
 * $Id$
 */

#include "Server.h"
#include <iostream>

Server::Server(int port):TCPConnection()
{
  listen(port);
}

Server::~Server()
{

}


void Server::check(){

  fd_set setfd;
  int retval;
  struct timeval tv;
  
  FD_ZERO(&setfd);
  /* insert File Descriptors to listen */
  FD_SET(socketID,&setfd);
  
  tv.tv_sec=0;
  tv.tv_usec=0;
  
  retval=select(socketID+1,&setfd,NULL,NULL,&tv);
  if(FD_ISSET(socketID,&setfd)){
    std::cout << "NEW CONNECTION" << std::endl;
    accept();
    
  }else{
    
    
  }
  
}
