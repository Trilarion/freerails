/** $Id$
  */

#include "CityController.h"

CityController::CityController (): Controller(NULL, GameElement::idCity)
{

}

CityController::~CityController ()
{

}

GameElement* CityController::CreateElement(Serializer* _serializer)
{
  City* city = new City(0,0,"");
  city->serialize(_serializer);
  return city;
}
