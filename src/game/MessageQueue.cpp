/** $Id$
  */

#include "MessageQueue.h"

MessageQueue::MessageQueue()
{
}

MessageQueue::~MessageQueue()
{

}

bool MessageQueue::hasMoreElements()
{
  return !empty();
}

void MessageQueue::addMsg(Message* msg)
{
  push(msg);
}

Message* MessageQueue::getMsg()
{
  Message* msg=front();
  pop();
  return msg;
}
