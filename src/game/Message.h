/** $Id$
  */
 
#ifndef __MESSAGE_H__
#define __MESSAGE_H__

#include "Player.h"

class Message {
public:

  enum MsgTypes { addElement=1, changeElement, deleteElement, stateOfLastMsg,
                  stateOfGame
		};

  /** Constructor */
  Message(int _type, long int _msgID, void* _data);
  Message(int _type, long int _msgID, void* _data, Player* _player);
  /** Destructor */
  virtual ~Message();
  
  int getType() { return type; };
  long int getMsgID() { return msgID;};
  void* getData() { return data;};
  Player* getPlayer() { return player;};
  
private:

  int type;
  long int msgID;
  void* data;
  Player* player;
};

#endif // __Message_H__