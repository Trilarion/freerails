/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

#include "TrainController.h"

TrainController::TrainController(WorldMap *_map) : Controller(_map, GameElement::idTrain)
{
}

TrainController::~TrainController()
{
}

GameElement* TrainController::CreateElement(Serializer* _serializer)
{
}

bool TrainController::canBuildElement(GameElement* _element)
{
  Train* train = (Train*)_element;
  return testBuildElement(train->getPosX(), train->getPosY());
}

bool TrainController::testBuildElement(int x, int y)
{
  Station* station = worldMap->getMapField(x,y)->getStation();
  if (station!=NULL)
  {
    if (station->getSize()!=Station::Signal) return true;
  }
  return false;
}

void TrainController::addGameElement(GameElement* _element)
{
  Train* train = (Train*)_element;
  trainDoBuild(train);
}

void TrainController::removeGameElement(long int _elementID)
{
}

void TrainController::trainDoBuild(Train* train)
{
  std::cerr << "BuildTrain: x, y: " << train->getPosX() << ":" << train->getPosY() << std::endl;
  Controller::addGameElement(train);
}

