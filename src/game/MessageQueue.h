/** $Id$
  */
 
#ifndef __MESSAGEQUEUE_H__
#define __MESSAGEQUEUE_H__

#include "Message.h"
#include <vector>

class MsgElement {

public:
  Message* Msg;
  MsgElement* next;

};

class MessageQueue {
public:

  /** Constructor */
  MessageQueue();
  /** Destructor */
  virtual ~MessageQueue();
  
  bool hasMoreElements();
  void addMsg(Message* msg);
  Message* getMsg();
  
private:

  MsgElement* first;
  MsgElement* last;

};

#endif // __MessageQueue_H__