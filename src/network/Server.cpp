/*
 * $Id$
 */

#include "Server.h"
#include "NetInitGame.h"
#include "FreeRailsLog.h"

Server::Server(int port):TCPConnection()
{
  listen(port);
  maxPlayers=3; /* */
  playersInGame=1; /* we're playing, are we???(ghfg) */
}

Server::~Server()
{

}


void Server::check(){

 
  int retval;
  struct timeval tv;

  tv.tv_sec=0;
  tv.tv_usec=0;
  
  buildFD_SET();
  retval=select(maxfd+1,&setfd,NULL,NULL,&tv);
  if(FD_ISSET(socketID,&setfd)){
    FreeRailsLog("Server: NEW CONNECTION");
    newPlayer();
  }else{
    
    iterate_client_list(clients,client);
    
    if(FD_ISSET(client->getConnectionId(),&setfd)){
      FreeRailsLog("Server:check(): Client trying to communicate");
    }
    
    iterate_client_list_end;
  }
  
  /*  FreeRailsLog("Server: check() END"); */
  
}

void Server::buildFD_SET(){

  Client* client;
  int fd;

  maxfd=socketID;

  FD_ZERO(&setfd);
  /* insert File Descriptors to listen */
  FD_SET(socketID,&setfd);

  iterate_client_list(clients, client);

  FD_SET(client->getConnectionId(),&setfd);
  if(client->getConnectionId() > maxfd)
    maxfd=client->getConnectionId();

  iterate_client_list_end;

}


void Server::newPlayer(){

  Client *client;
  int fd;

  FreeRailsLog("Server:newPlayer(): ONE CLIENT");
  fd=accept();

  /*  
      if(playersInGame>=maxPlayers)
      playersInGame++;
  */

  client=new Client(fd);  
  clients.push_back(client); /* insert Client */

}
