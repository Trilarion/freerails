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

int Controller::computeDirection(int x, int y)
{
  int dir;
  
  if(x < 10)
  {
    if(y < 10)
    {
      dir = 8;
    }
    else
    {
      if(y < 20)
      {
        dir = 7;
      }
      else
      {
        dir = 6;
      }
    }
  }
  else
  {
    if(x < 20)
    {
      if(y < 10)
      {
        dir = 1;
      }
      else
      {
        if(y < 20)
        {
          dir = -1;   // here we can't get an direction
        }
        else
        {
          dir = 5;
        }
      }
    }
    else
    {
      if(y < 10)
      {
        dir = 2;
      }
      else
      {
        if(y < 20)
        {
          dir = 3;
        }
        else
        {
          dir = 4;
        }
      }
    }
  }
//  if(x < -5)
//  {
//    if(y < -5)
//    {
//      dir = 8;
//    }
//    else
//    {
//      if(y < 6)
//      {
//        dir = 7;
//      }
//      else
//      {
//        dir = 6;
//      }
//    }
//  }
//  else
//  {
//    if(x < 6)
//    {
//      if(y > 5)
//      {
//        dir = 5;
//      }
//      else
//      {
//        dir = 1;
//      }
//    }
//    else
//    {
//      if(y < -5)
//      {
//        dir = 2;
//      }
//      else
//      {
//        if(y < 6)
//        {
//          dir = 3;
//        }
//        else
//        {
//          dir = 4;
//        }
//      }
//    }
//  }
  return dir;
}
