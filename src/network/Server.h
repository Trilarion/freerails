/*
 * $Id$
 */

#ifndef __SERVER_H__
#define __SERVER_H__

#include "TCPConnection.h"
#include "Client.h"
#include "Serializer.h"
/* #include "FreeHash.h" */
#include <list>

class Server: public TCPConnection {

 public:

  Server(int port);
  /**  */
  ~Server();
    

  void check();
    
 private:
    
  /* void connect(char* host, int port); */
  std::list<Client *> clients;
  short playersInGame; /* counts the current number of players in the game */
  short maxPlayers;
  fd_set setfd;
  short maxfd;
    
  void newPlayer();
  void buildFD_SET();
    
};


#define iterate_client_list(clients, client){ \
  std::list<Client *>::iterator iter,it; \
  Client *client; \
  iter=clients.begin(); \
  while( ++iter != clients.end() ){ \
    it=iter++; \
    client=*it;

#define iterate_client_list_end \
  } \
}



#endif
