/** $Id$
  */

#include "ControllerDispatcher.h"

ControllerDispatcher::ControllerDispatcher () {

}

ControllerDispatcher::~ControllerDispatcher () {

}

void ControllerDispatcher::addController (Controller* _controller) {

  int typeID = _controller->getTypeID();
  controllerMap[typeID] = _controller;

}

Controller* ControllerDispatcher::getController (int _typeID) {

  map<int,Controller*>::iterator it;
  it = controllerMap.find(_typeID);
  if (it != controllerMap.end())
    return (*it).second;
  else return NULL;

}

