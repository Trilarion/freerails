/** $Id$
  */

#include "Controller.h"

Controller::Controller (int _typeID) {

  typeID = _typeID;

}

Controller::~Controller () {

}

void Controller::addGameElement (GameElement* _element) {

  long int elementID = _element->getElementID();
  elementMap[elementID] = _element;  

}

GameElement* Controller::getGameElement (long int _elementID) {

  map<long int, GameElement*>::iterator it;
  it = elementMap.find(_elementID);
  if ( it != elementMap.end())
    return (*it).second;
  else return NULL;

}

void Controller::removeGameElement (long int _elementID) {

  elementMap.erase(_elementID);
}