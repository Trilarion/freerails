/** $Id$
  */
 
#ifndef __MESSAGE_H__
#define __MESSAGE_H__

#include "GameElement.h"
#include "Player.h"

class Message
{
  public:

    enum MsgTypes { addElement=1, changeElement, deleteElement, stateOfLastMsg,
                    stateOfGame };

    /** Constructor */
    Message(MsgTypes _msgID, GameElement::TypeID _typeID, void* _data);
    Message(MsgTypes _msgID, GameElement::TypeID _typeID, void* _data, Player* _player);
    /** Destructor */
    virtual ~Message();

    GameElement::TypeID getType() { return typeID; };
    MsgTypes getMsgID() { return msgID;};
    void* getData() { return data;};
    Player* getPlayer() { return player;};

  private:

    GameElement::TypeID typeID;
    MsgTypes msgID;
    void* data;
    Player* player;
};

#endif // __Message_H__
