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
/*  int x, y;
  int x1, y1;

  x1 = ((station_data*)(_data))->field_pos_x;
  y1 = ((station_data*)(_data))->field_pos_y;
  x = x1 / 30;
  y = y1 / 30;
  x1 -= (x * 30);
  y1 -= (y * 30);

  *xx = x;
  *yy = y;
  *dd = 0;

  #warning complete me
//  if (data->player->getMoney() < 1000)
//  {
//    SetMessage("You have not enough Money to build track there!");
//    return 0;
//  }

  return testBuildElement(x, y);
*/
  return false;
}

bool StationController::testBuildElement(int x, int y)
{
  MapField *field;
  Track *track;
  unsigned int connect;

  field = worldMap->getMapField(x, y);
  if (field == NULL)
    return false;
  track = field->getTrack();
  if (track != NULL )
  {
    connect = track->getConnect();
    #warning fix me
    if (!(connectIsBuildable(connect | TrackIsBlocked)))
    {
//      SetMessage("Can't set such track there!");
      return false;
    }
  }
  if (field->isWater())
  {
//    SetMessage("You can't build normal track's on Water!");
    return false;
  }
  return true;
}

void StationController::addGameElement(void *_data)
{
  int x, y, dir;

  if(!(canBuildElement((GameElement*) _data)))
    return;
  stationDoBuild(x, y);
}

void StationController::removeGameElement(void */*_data*/)
{
}

bool StationController::connectIsBuildable(unsigned int connect)
{
  bool status;
  int c = connect & 0x000000ff;

  if (connect & (TrackIsBlocked)) // Hier muß glaube getestet werden ob up || downgrade able
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

void StationController::stationDoBuild(int x, int y)
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
//    PlayerMoneyDecrement(ThisPlayer,1000);
}

//void TrackBuildDrawPanelInfo() {
//
//    DrawText(PanelInfo.Box.X+2,PanelInfo.Box.Y+2,SmallFont,"Build: Track");
//    DrawText(PanelInfo.Box.X+2,PanelInfo.Box.Y+17,SmallFont,"Cost: 1.000 $");
//    DrawText(PanelInfo.Box.X+2,PanelInfo.Box.Y+32,SmallFont,"Monthly Cost: 1.000 $");
//
//}
