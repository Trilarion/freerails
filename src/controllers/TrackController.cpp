/***************************************************************************
                          trackcontroller.cpp  -  description
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

#include "TrackController.h"

TrackController::TrackController(WorldMap *_map) : Controller(_map, GameElement::idTrack)
{
}

TrackController::~TrackController()
{
}

GameElement* TrackController::CreateElement(Serializer* _serializer)
{
}

bool TrackController::canBuildElement(GameElement* _element)
{
  return false;
/*  if (!testBuildElement(x,y,dir))
  {
    return false;
  }

  switch (dir)
  {
    case 1:
      y--;
      dir = 5;
      break;
    case 2:
      y--;
      x++;
      dir = 6;
      break;
    case 3:
      x++;
      dir = 7;
      break;
    case 4:
      y++;
      x++;
      dir = 8;
      break;
    case 5:
      y++;
      dir = 1;
      break;
    case 6:
      y++;
      x--;
      dir = 2;
      break;
    case 7:
      x--;
      dir = 3;
      break;
    case 8:
      y--;
      x--;
      dir = 4;
      break;
  };
  return testBuildElement(x,y,dir);
*/
}

bool TrackController::testBuildElement(int x, int y, int dir)
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
    connect = doConnect(track->getConnect(), dir);
    if (!(connectIsBuildable(connect)))
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

void TrackController::addGameElement(void *_data)
{
  int x, y, dir;
  
//  if(!(canBuildElement(_data, &x, &y, &dir)))
//    return;
  trackDoBuild(x, y, dir);
}

void TrackController::removeGameElement(void */*_data*/)
{
}

bool TrackController::connectIsBuildable(unsigned int connect)
{
  int c = connect & 0x000000ff;
  bool status;

  if (connect & (TrackIsBlocked))
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
  else
  {
    switch (c)
    {
      case TrackGoNorth:
      case TrackGoNorthEast:
      case TrackGoEast:
      case TrackGoSouthEast:
      case TrackGoSouth:
      case TrackGoSouthWest:
      case TrackGoWest:
      case TrackGoNorthWest:
      case TrackGoNorth | TrackGoSouth:
      case TrackGoNorthEast | TrackGoSouthWest:
      case TrackGoEast | TrackGoWest:
      case TrackGoSouthEast | TrackGoNorthWest:
      case TrackGoNorth | TrackGoSouthEast:
      case TrackGoNorthEast | TrackGoSouth:
      case TrackGoEast | TrackGoSouthWest:
      case TrackGoWest | TrackGoSouthEast:
      case TrackGoNorthWest | TrackGoSouth:
      case TrackGoNorth | TrackGoSouthWest:
      case TrackGoNorthEast | TrackGoWest:
      case TrackGoNorthWest | TrackGoEast:
      case TrackGoNorth | TrackGoSouth | TrackGoSouthEast:
      case TrackGoNorthEast | TrackGoSouth | TrackGoSouthWest:
      case TrackGoEast | TrackGoSouthWest | TrackGoWest:
      case TrackGoNorthWest | TrackGoSouthEast | TrackGoWest:
      case TrackGoNorth | TrackGoSouth | TrackGoNorthWest:
      case TrackGoNorth | TrackGoNorthEast | TrackGoSouthWest:
      case TrackGoNorthEast | TrackGoEast | TrackGoWest:
      case TrackGoEast | TrackGoSouthEast | TrackGoNorthWest:
      case TrackGoNorth | TrackGoSouth | TrackGoSouthWest:
      case TrackGoNorthEast | TrackGoSouthWest | TrackGoWest:
      case TrackGoNorthWest | TrackGoEast | TrackGoWest:
      case TrackGoNorth | TrackGoSouthEast | TrackGoNorthWest:
      case TrackGoNorth | TrackGoNorthEast | TrackGoSouth:
      case TrackGoNorthEast | TrackGoEast | TrackGoSouthWest:
      case TrackGoEast | TrackGoSouthEast | TrackGoWest:
      case TrackGoNorthWest | TrackGoSouthEast | TrackGoSouth:
      case TrackGoNorth | TrackGoEast | TrackGoSouth | TrackGoWest:
      case TrackGoNorthEast | TrackGoSouthEast | TrackGoSouthWest | TrackGoNorthWest:
      case TrackGoNorth | TrackGoEast:
      case TrackGoNorthEast | TrackGoSouthEast:
      case TrackGoEast | TrackGoSouth:
      case TrackGoSouthEast | TrackGoSouthWest:
      case TrackGoSouth | TrackGoWest:
      case TrackGoSouthWest | TrackGoNorthWest:
      case TrackGoNorth | TrackGoWest:
      case TrackGoNorthWest | TrackGoNorthEast:
      case TrackGoNorth | TrackGoNorthEast:
      case TrackGoNorthEast | TrackGoEast:
      case TrackGoEast | TrackGoSouthEast:
      case TrackGoSouthEast | TrackGoSouth:
      case TrackGoSouth | TrackGoSouthWest:
      case TrackGoSouthWest | TrackGoWest:
      case TrackGoWest | TrackGoNorthWest:
      case TrackGoNorthWest | TrackGoNorth:
      case TrackGoNorth | TrackGoEast | TrackGoWest:
      case TrackGoNorthEast | TrackGoSouthEast | TrackGoNorthWest:
      case TrackGoNorth | TrackGoEast | TrackGoSouth:
      case TrackGoNorthEast | TrackGoSouthEast | TrackGoSouthWest:
      case TrackGoEast | TrackGoSouth | TrackGoWest:
      case TrackGoSouthEast | TrackGoSouthWest | TrackGoNorthWest:
      case TrackGoNorth | TrackGoSouth | TrackGoWest:
      case TrackGoNorthEast | TrackGoSouthWest | TrackGoNorthWest:
        status = true;
        break;
      default:
        status = false;
        break;
    }
  }
  return status;
}

unsigned int TrackController::doConnect(unsigned int connect, int dir)
{
  switch (dir)
  {
    case 1:
      connect |= TrackGoNorth;
      break;
    case 2:
      connect |= TrackGoNorthEast;
      break;
    case 3:
      connect |= TrackGoEast;
      break;
    case 4:
      connect |= TrackGoSouthEast;
      break;
    case 5:
      connect |= TrackGoSouth;
      break;
    case 6:
      connect |= TrackGoSouthWest;
      break;
    case 7:
      connect |= TrackGoWest;
      break;
    case 8:
      connect |= TrackGoNorthWest;
      break;
  };
  return connect;
}

void TrackController::trackUpdate(int x, int y, int dir)
{
  MapField *field;
  Track *track;
  unsigned int connect;
  int connectTo;

  switch (dir)
  {
    case 1:
      y--;
      connectTo = 5;
      break;
    case 2:
      y--;
      x++;
      connectTo = 6;
      break;
    case 3:
      x++;
      connectTo = 7;
      break;
    case 4:
      y++;
      x++;
      connectTo = 8;
      break;
    case 5:
      y++;
      connectTo = 1;
      break;
    case 6:
      y++;
      x--;
      connectTo = 2;
      break;
    case 7:
      x--;
      connectTo = 3;
      break;
    case 8:
      y--;
      x--;
      connectTo = 4;
      break;
  };

  field = worldMap->getMapField(x, y);
  if (field == NULL)
    return;
  track = field->getTrack();
  if (track == NULL )
  {
    #warning complete me
    track = new Track(x, y, NULL, 0);
    field->setTrack(track);
  }
  connect = track->getConnect();
  connect = doConnect(connect, connectTo);
  track->setConnect(connect);
//  setCorner(x, y);
}

void TrackController::trackDoBuild(int x,int y, int dir)
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
    track = new Track(x, y, NULL, 0);
    field->setTrack(track);
  }
  connect = doConnect(track->getConnect(), dir);
  track->setConnect(connect);
//  setCorner(x, y);
  trackUpdate(x, y, dir);
//    PlayerMoneyDecrement(ThisPlayer,1000);
}

/*
void TrackController::setCorner(int x, int y)
{
  MapField *field, *field2;
  Track *track, *track2;
  unsigned int connect, connect2;

  field = worldMap->getMapField(x, y);
  if (field == NULL)
    return;
  track = field->getTrack();
  connect = track->getConnect();
  if (connect & TrackGoNorthEast)
  {
    field2 = worldMap->getMapField(x + 1, y);
    if (field2 != NULL)
    {
      track2 = field2->getTrack();
      if (track2 == NULL )
      {
        #warning complete me
        track2 = new Track(NULL, NULL);
        field2->setTrack(track2);
      }
      connect2 = track2->getConnect();
      if (!(connect2 & TrackHasCornerNorthWest))
      {
        connect2 |= TrackHasCornerNorthWest;
        track2->setConnect(connect2);
      }
    }
  }
  if (connect & TrackGoSouthEast)
  {
    field2 = worldMap->getMapField(x + 1, y);
    if (field2 != NULL)
    {
      track2 = field2->getTrack();
      if (track2 == NULL )
      {
        #warning complete me
        track2 = new Track(NULL, NULL);
        field2->setTrack(track2);
      }
      connect2 = track2->getConnect();
      if (!(connect2 & TrackHasCornerSouthWest))
      {
        connect2 |= TrackHasCornerSouthWest;
        track2->setConnect(connect2);
      }
    }
  }
  if (connect & TrackGoSouthWest)
  {
    field2 = worldMap->getMapField(x - 1, y);
    if (field2 != NULL)
    {
      track2 = field2->getTrack();
      if (track2 == NULL )
      {
        #warning complete me
        track2 = new Track(NULL, NULL);
        field2->setTrack(track2);
      }
      connect2 = track2->getConnect();
      if (!(connect2 & TrackHasCornerSouthEast))
      {
        connect2 |= TrackHasCornerSouthEast;
        track2->setConnect(connect2);
      }
    }
  }
  if (connect & TrackGoNorthWest)
  {
    field2 = worldMap->getMapField(x - 1, y);
    if (field2 != NULL)
    {
      track2 = field2->getTrack();
      if (track2 == NULL )
      {
        #warning complete me
        track2 = new Track(NULL, NULL);
        field2->setTrack(track2);
      }
      connect2 = track2->getConnect();
      if (!(connect2 & TrackHasCornerNorthEast))
      {
        connect2 |= TrackHasCornerNorthEast;
        track2->setConnect(connect2);
      }
    }
  }
}
*/

//void TrackBuildDrawPanelInfo() {
//
//    DrawText(PanelInfo.Box.X+2,PanelInfo.Box.Y+2,SmallFont,"Build: Track");
//    DrawText(PanelInfo.Box.X+2,PanelInfo.Box.Y+17,SmallFont,"Cost: 1.000 $");
//    DrawText(PanelInfo.Box.X+2,PanelInfo.Box.Y+32,SmallFont,"Monthly Cost: 1.000 $");
//
//}
