/** $Id$
  */

#include "Message.h"

Message::Message(MsgTypes _msgType, long int _msgID, void* _data)
{
  msgType = _msgType;
  msgID = _msgID;
  data = _data;
  player = NULL;
}

Message::Message(MsgTypes _msgType, long int _msgID, void* _data, Player* _player)
{
  msgType = _msgType;
  msgID = _msgID;
  data = _data;
  player = _player;
}

Message::~Message()
{
}
