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
    Message(MsgTypes _msgType, long int _msgID, void* _data);
    Message(MsgTypes _msgType, long int _msgID, void* _data, Player* _player);
    /** Destructor */
    virtual ~Message();

    MsgTypes getMsgType() { return msgType;};
    long int getMsgID() { return msgID; };
    void* getData() { return data;};
    Player* getPlayer() { return player;};

  private:

    MsgTypes msgType;
    long int msgID;
    void* data;
    Player* player;
};

#endif // __Message_H__
