/** $Id$
  */

#include "MessageQueue.h"

MessageQueue::MessageQueue() {

  first=NULL;
  last=NULL;
}

MessageQueue::~MessageQueue() {

}

bool MessageQueue::hasMoreElements() {

  if (first!=NULL) return true;
    else return false;
}

void MessageQueue::addMsg(Message* msg) {

  MsgElement* newMsgElement = new MsgElement;
  newMsgElement->Msg=msg;
  newMsgElement->next=NULL;
  if (first==NULL) {
    first=newMsgElement;
    last =newMsgElement;
  } else {
    last->next=newMsgElement;
    last=newMsgElement;
  }
}

Message* MessageQueue::getMsg() {

  if (first==NULL) return NULL;
  Message* msg=first->Msg;
  if (first==last) {
    delete first;
    first=NULL;
    last=NULL;
  } else {
    MsgElement* help=first->next;
    delete first;
    first=help;
  }
  
  return msg;
}