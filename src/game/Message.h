/** $Id$
  */
 
#ifndef __MESSAGE_H__
#define __MESSAGE_H__


class Message {
public:

  enum MsgTypes { startGame=0, stopGame, pauseGame,

                  newDay=100
		};

  /** Constructor */
  Message(int _type, void* _data);
  /** Destructor */
  virtual ~Message();
  
  int getType() { return type; };
  void* getData() { return data;};
  
private:

  int type;
  void* data;
};

#endif // __Message_H__