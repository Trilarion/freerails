/*
 * $Id$
 */

#include "Client.h"
#include "Network.h"
#include "FreeRailsLog.h"

#include <iostream>

/* #include "ConnectGame.h" */

Client::Client():TCPConnection() {

}

Client::Client(int _socketID):TCPConnection(_socketID) {

}


Client::~Client() {

}


bool Client::joinGame(Player *_player, Serializer *_serializer)
{
  
  /*  RequestJoinGame jg((char *)(_player->getName()).c_str(),0,7); */
  FreeRailsLog("Client: Init Connection");
  _serializer->initSend(MSG_REQUEST_JOIN_GAME);
  *_serializer << _player->getName();
  _serializer->finishSend(this);
}
