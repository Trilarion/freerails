/** $Id$
  */

#include "Message.h"

Message::Message(int _type, long int _msgID, void* _data) {

  type=_type;
  msgID=_msgID;
  data=_data;
  player=NULL;
}

Message::Message(int _type, long int _msgID, void* _data, Player* _player) {

  type=_type;
  msgID=_msgID;
  data=_data;
  player=_player;
}

Message::~Message() {

}
