/** $Id$
  */

#include "ControllerDispatcher.h"

ControllerDispatcher::ControllerDispatcher () {

}

ControllerDispatcher::~ControllerDispatcher () {

}

void ControllerDispatcher::addController (Controller* _controller) {

  controllerMap[_controller->getTypeID()] = _controller;

}

Controller* ControllerDispatcher::getController (GameElement::TypeID _typeID) {

  std::map<GameElement::TypeID,Controller*>::iterator it;
  it = controllerMap.find(_typeID);
  if (it != controllerMap.end())
    return (*it).second;
  else return NULL;

}
