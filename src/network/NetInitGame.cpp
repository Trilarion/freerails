#include "NetInitGame.h"

/*************************************************************/
/*                  PacketRequestJoinGame                    */
/*************************************************************/

PacketRequestJoinGame::PacketRequestJoinGame(){

}

PacketRequestJoinGame::~PacketRequestJoinGame(){

}

void PacketRequestJoinGame::serialize(Serializer* _serializer){
  _serializer->initSend(MSG_REQUEST_JOIN_GAME);
  
  *_serializer << (const std::string)name;

  /* _serializer->finishSend(); */
}

void PacketRequestJoinGame::deserialize(Serializer* _serializer){

  /* we assume that the packet has been read and it's ok 
     to retrieve the data (that is how it should be) */
  
  *_serializer >> name;

}


/*************************************************************/
/*                PacketReplyRequestJoinGame                 */
/*************************************************************/


PacketReplyRequestJoinGame::PacketReplyRequestJoinGame(){

}

PacketReplyRequestJoinGame::~PacketReplyRequestJoinGame(){

}

void PacketReplyRequestJoinGame::serialize(Serializer* _serializer){

  _serializer->initSend(MSG_REPLY_REQUEST_JOIN_GAME);
  
  *_serializer << answer;
  
}


void PacketReplyRequestJoinGame::deserialize(Serializer* _serializer){
  
  /* we assume that the packet has been read and it's ok 
     to retrieve the data (that is how it should be) */
  *_serializer >> answer;
  
}

/*************************************************************/
/*                        PacketMapInfo                      */
/*************************************************************/

