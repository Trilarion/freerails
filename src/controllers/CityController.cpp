/** $Id$
  */

#include "CityController.h"

CityController::CityController(WorldMap* _map): Controller(_map, GameElement::idCity)
{

}

CityController::~CityController()
{

}

GameElement* CityController::CreateElement(Serializer* _serializer)
{
  City* city = new City(0,0,"");
  city->serialize(_serializer);
  return city;
}
