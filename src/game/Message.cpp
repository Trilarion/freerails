/** $Id$
  */

#include "Message.h"

Message::Message(MsgTypes _msgID, GameElement::TypeID _typeID, void* _data)
{
  typeID = _typeID;
  msgID = _msgID;
  data = _data;
  player = NULL;
}

Message::Message(MsgTypes _msgID, GameElement::TypeID _typeID, void* _data, Player* _player)
{
  typeID = _typeID;
  msgID = _msgID;
  data = _data;
  player = _player;
}

Message::~Message()
{
}
