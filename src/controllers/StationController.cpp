/***************************************************************************
                          stationcontroller.cpp  -  description
                             -------------------
    begin                : Don Sep 19 2002
    copyright            : (C) 2002 by Frank Schmischke
    email                : frank.schmischke@t-online.de
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
/** $Id$
 */

#include "StationController.h"

StationController::StationController(WorldMap *_map) : Controller(_map, GameElement::idStation)
{
}

StationController::~StationController()
{
}

GameElement* StationController::CreateElement(Serializer* _serializer)
{
}

bool StationController::canBuildElement(GameElement* _element)
{
  Station* station = (Station*) _element;
  return testBuildElement(station->getPosX(), station->getPosY());
}

bool StationController::testBuildElement(int x, int y)
{
  MapField *field;
  Track *track;

  field = worldMap->getMapField(x, y);
  if (field == NULL) return false;
  track = field->getTrack();
  if (track != NULL )
  {
    return connectIsBuildable(track->getConnect());
  }
  return false;
}

void StationController::addGameElement(GameElement* _element)
{
  Station* station = (Station*)_element;
  stationDoBuild(station->getPosX(), station->getPosY(), station->getSize());
}

void StationController::removeGameElement(long int _elementID)
{
}

bool StationController::connectIsBuildable(unsigned int connect)
{
  bool status;
  int c = connect;

  if (connect & TrackIsBlocked)
    status = false;
  else
  {
    switch (c)
    {
      case TrackGoNorth:
      case TrackGoNorth | TrackGoSouth:
      case TrackGoSouth:
      case TrackGoNorthEast:
      case TrackGoNorthEast | TrackGoSouthWest:
      case TrackGoSouthWest:
      case TrackGoEast:
      case TrackGoEast | TrackGoWest:
      case TrackGoWest:
      case TrackGoSouthEast:
      case TrackGoSouthEast | TrackGoNorthWest:
      case TrackGoNorthWest:
        status = true;
        break;
      default:
        status = false;
        break;
    }
  }
  return status;
}

void StationController::stationDoBuild(int x, int y, int size)
{
  MapField *field;
  Track *track;
  unsigned int connect;

  field = worldMap->getMapField(x, y);
  if (field == NULL)
    return;
  track = field->getTrack();
  if (track == NULL )
  {
    #warning complete me
    return;
  }
  connect = track->getConnect();
/*  if (connect & TrackIsSignal)
    connect &= ~TrackIsSignal; Verstehe Gott?? *gg* */
  #warning complete me
  connect |= TrackIsBlocked;
  track->setConnect(connect);
}
