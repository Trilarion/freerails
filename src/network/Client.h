/*
 * $Id$
 */

#ifndef __CLIENT_H__
#define __CLIENT_H__

#include "TCPConnection.h"
#include "Player.h"
#include "Serializer.h"

class Client: public TCPConnection {
  
 public:
  
  Client();
  Client(int _socketID);
  /**  */
  ~Client();
  
  bool joinGame(Player *_player, Serializer *_serializer);
  
 private:
  
  /* void listen(int port); */
  /* int accept(); */
  
};

#endif
