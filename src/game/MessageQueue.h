/** $Id$
  */
 
#ifndef __MESSAGEQUEUE_H__
#define __MESSAGEQUEUE_H__

#include "Message.h"
#include <queue>

class MessageQueue: public std::queue<Message*> {
public:

  /** Constructor */
  MessageQueue();
  /** Destructor */
  virtual ~MessageQueue();
  
  bool hasMoreElements();
  void addMsg(Message* msg);
  Message* getMsg();

};

#endif // __MessageQueue_H__
