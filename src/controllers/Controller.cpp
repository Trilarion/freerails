/** $Id$
  */

#include "Controller.h"

Controller::Controller (WorldMap *_map, GameElement::TypeID _typeID)
{
  worldMap = _map;
  typeID = _typeID;
  highestElementID = 0;
}

Controller::~Controller ()
{

}

void Controller::addGameElement (GameElement* _element)
{
  _element->setElementID(++highestElementID);
  elementMap[highestElementID] = _element;  
}

GameElement* Controller::getGameElement (long int _elementID)
{
  std::map<GameElement::ElementID, GameElement*>::iterator it;
  it = elementMap.find(_elementID);
  if ( it != elementMap.end())
    return (*it).second;
  else
    return NULL;
}

void Controller::removeGameElement (long int _elementID)
{
  elementMap.erase(_elementID);
}
