/*
 * $Id$
 */


#include "GameController.h"
#include "Player.h"
#include "Track.h"

Track::Track(GameController* _controller, Player* _player) : GameElement(_player, idTrack)
{
  int i;
  
  controller = _controller;
  connect = 0;
  for(i=0;i<5;i++)
  {
    offset_x[i] = 0;
    offset_y[i] = 0;
  }
}

Track::~Track()
{
}

void Track::setConnect(unsigned short _con)
{
  connect = _con;
  int xp, yp;
  unsigned short c;

  c = connect & 0x00ff;   // we are intersted in trackdirection only

  if (connect & (TrackIsStation | TrackIsBridge | TrackIsSignal))
  {
    #warning complete me
    switch (c)
    {
      case TrackGoNorth:
        xp = 9;
        yp = 0;
        break;
      case TrackGoNorthEast:
        xp = 10;
        yp = 0;
        break;
     case TrackGoEast:
        xp = 11;
        yp = 0;
        break;
      case TrackGoSouthEast:
        xp = 12;
        yp = 0;
        break;
      case TrackGoSouth:
        xp = 13;
        yp = 0;
        break;
      case TrackGoSouthWest:
        xp = 14;
        yp = 0;
        break;
      case TrackGoWest:
        xp = 15;
        yp = 0;
        break;
      case TrackGoNorthWest:
        xp = 16;
        yp = 0;
        break;
       case TrackGoNorth | TrackGoSouth:
        xp = 9;
        yp = 1;
        break;
      case TrackGoNorthEast | TrackGoSouthWest:
        xp = 10;
        yp = 1;
        break;
      case TrackGoEast | TrackGoWest:
        xp = 11;
        yp = 1;
        break;
      case TrackGoSouthEast | TrackGoNorthWest:
        xp = 12;
        yp = 1;
        break;
      default:
        xp = -1;
        yp = -1;
    }
    if (connect & TrackIsBridge)
    {
      #warning complete me (missing wood, stone, steel)
      yp += 2;
    }
    else
    {
      if (connect & TrackIsStation)
      {
        #warning complete me (missing depot, station, terminal)
        yp += 10;
      }
      else
        yp += 8;
    }
  }
  else
  {
    switch (c)
    {
      case TrackGoNorth:
        xp = 0;
        yp = 0;
        break;
      case TrackGoNorthEast:
        xp = 1;
        yp = 0;
        break;
      case TrackGoEast:
        xp = 2;
        yp = 0;
        break;
      case TrackGoSouthEast:
        xp = 3;
        yp = 0;
        break;
      case TrackGoSouth:
        xp = 4;
        yp = 0;
        break;
      case TrackGoSouthWest:
        xp = 5;
        yp = 0;
        break;
      case TrackGoWest:
        xp = 6;
        yp = 0;
        break;
      case TrackGoNorthWest:
        xp = 7;
        yp = 0;
        break;
      case TrackGoNorth | TrackGoSouth:
        xp = 0;
        yp = 1;
        break;
      case TrackGoNorthEast | TrackGoSouthWest:
        xp = 1;
        yp = 1;
        break;
      case TrackGoEast | TrackGoWest:
        xp = 2;
        yp = 1;
        break;
      case TrackGoSouthEast | TrackGoNorthWest:
        xp = 3;
        yp = 1;
        break;
      case TrackGoNorth | TrackGoSouthEast:
        xp = 0;
        yp = 2;
        break;
      case TrackGoNorthEast | TrackGoSouth:
        xp = 1;
        yp = 2;
        break;
      case TrackGoEast | TrackGoSouthWest:
        xp = 2;
        yp = 2;
        break;
      case TrackGoWest | TrackGoSouthEast:
        xp = 3;
        yp = 2;
        break;
      case TrackGoNorthWest | TrackGoSouth:
        xp = 4;
        yp = 2;
        break;
      case TrackGoNorth | TrackGoSouthWest:
        xp = 5;
        yp = 2;
        break;
      case TrackGoNorthEast | TrackGoWest:
        xp = 6;
        yp = 2;
        break;
      case TrackGoNorthWest | TrackGoEast:
        xp = 7;
        yp = 2;
        break;
      case TrackGoNorth | TrackGoSouth | TrackGoSouthEast:
        xp = 0;
        yp = 3;
        break;
      case TrackGoNorthEast | TrackGoSouth | TrackGoSouthWest:
        xp = 1;
        yp = 3;
        break;
      case TrackGoEast | TrackGoSouthWest | TrackGoWest:
        xp = 2;
        yp = 3;
        break;
      case TrackGoNorthWest | TrackGoSouthEast | TrackGoWest:
        xp = 3;
        yp = 3;
        break;
      case TrackGoNorth | TrackGoSouth | TrackGoNorthWest:
        xp = 4;
        yp = 3;
        break;
      case TrackGoNorth | TrackGoNorthEast | TrackGoSouthWest:
        xp = 5;
        yp = 3;
        break;
      case TrackGoNorthEast | TrackGoEast | TrackGoWest:
        xp = 6;
        yp = 3;
        break;
      case TrackGoEast | TrackGoSouthEast | TrackGoNorthWest:
        xp = 7;
        yp = 3;
        break;
      case TrackGoNorth | TrackGoSouth | TrackGoSouthWest:
        xp = 0;
        yp = 4;
        break;
      case TrackGoNorthEast | TrackGoSouthWest | TrackGoWest:
        xp = 1;
        yp = 4;
        break;
      case TrackGoNorthWest | TrackGoEast | TrackGoWest:
        xp = 2;
        yp = 4;
        break;
      case TrackGoNorth | TrackGoSouthEast | TrackGoNorthWest:
        xp = 3;
        yp = 4;
        break;
      case TrackGoNorth | TrackGoNorthEast | TrackGoSouth:
        xp = 4;
        yp = 4;
        break;
      case TrackGoNorthEast | TrackGoEast | TrackGoSouthWest:
        xp = 5;
        yp = 4;
        break;
      case TrackGoEast | TrackGoSouthEast | TrackGoWest:
        xp = 6;
        yp = 4;
        break;
      case TrackGoNorthWest | TrackGoSouthEast | TrackGoSouth:
        xp = 7;
        yp = 4;
        break;
      case TrackGoNorth | TrackGoEast | TrackGoSouth | TrackGoWest:
        xp = 0;
        yp = 5;
        break;
      case TrackGoNorthEast | TrackGoSouthEast | TrackGoSouthWest | TrackGoNorthWest:
        xp = 1;
        yp = 5;
        break;

      case TrackGoNorth | TrackGoEast:
        xp = 0;
        yp = 6;
        break;
      case TrackGoNorthEast | TrackGoSouthEast:
        xp = 1;
        yp = 6;
        break;
      case TrackGoEast | TrackGoSouth:
        xp = 2;
        yp = 6;
        break;
      case TrackGoSouthEast | TrackGoSouthWest:
        xp = 3;
        yp = 6;
        break;
      case TrackGoSouth | TrackGoWest:
        xp = 4;
        yp = 6;
        break;
      case TrackGoSouthWest | TrackGoNorthWest:
        xp = 5;
        yp = 6;
        break;
      case TrackGoNorth | TrackGoWest:
        xp = 6;
        yp = 6;
        break;
      case TrackGoNorthWest | TrackGoNorthEast:
        xp = 7;
        yp = 6;
        break;
      case TrackGoNorth | TrackGoNorthEast:
        xp = 0;
        yp = 7;
        break;
      case TrackGoNorthEast | TrackGoEast:
        xp = 1;
        yp = 7;
        break;
      case TrackGoEast | TrackGoSouthEast:
        xp = 2;
        yp = 7;
        break;
      case TrackGoSouthEast | TrackGoSouth:
        xp = 3;
        yp = 7;
        break;
      case TrackGoSouth | TrackGoSouthWest:
        xp = 4;
        yp = 7;
        break;
      case TrackGoSouthWest | TrackGoWest:
        xp = 5;
        yp = 7;
        break;
      case TrackGoWest | TrackGoNorthWest:
        xp = 6;
        yp = 7;
        break;
      case TrackGoNorthWest | TrackGoNorth:
        xp = 7;
        yp = 7;
        break;
      case TrackGoNorth | TrackGoEast | TrackGoWest:
        xp = 0;
        yp = 8;
        break;
      case TrackGoNorthEast | TrackGoSouthEast | TrackGoNorthWest:
        xp = 1;
        yp = 8;
        break;
      case TrackGoNorth | TrackGoEast | TrackGoSouth:
        xp = 2;
        yp = 8;
        break;
      case TrackGoNorthEast | TrackGoSouthEast | TrackGoSouthWest:
        xp = 3;
        yp = 8;
        break;
      case TrackGoEast | TrackGoSouth | TrackGoWest:
        xp = 4;
        yp = 8;
        break;
      case TrackGoSouthEast | TrackGoSouthWest | TrackGoNorthWest:
        xp = 5;
        yp = 8;
        break;
      case TrackGoNorth | TrackGoSouth | TrackGoWest:
        xp = 6;
        yp = 8;
        break;
      case TrackGoNorthEast | TrackGoSouthWest | TrackGoNorthWest:
        xp = 7;
        yp = 8;
        break;


      default:
        xp = -1;
        yp = -1;
    }
  }
  offset_x[0] = xp * 60 + 15;
  offset_y[0] = yp * 60 + 15;

  if (connect & TrackHasCornerNorthEast)
  {
    offset_x[1] = 615;
    offset_y[1] = 975;
  }
  else
  {
    offset_x[1] = -1;
    offset_y[1] = -1;
  }

  if (connect & TrackHasCornerSouthEast)
  {
    offset_x[2] = 675;
    offset_y[2] = 975;
  }
  else
  {
    offset_x[2] = -1;
    offset_y[2] = -1;
  }

  if (connect & TrackHasCornerSouthWest)
  {
    offset_x[3] = 735;
    offset_y[3] = 975;
  }
  else
  {
    offset_x[3] = -1;
    offset_y[3] = -1;
  }

  if (connect & TrackHasCornerNorthWest)
  {
    offset_x[4] = 555;
    offset_y[4] = 975;
  }
  else
  {
    offset_x[4] = -1;
    offset_y[4] = -1;
  }
}

void Track::getTrackTile(int i, int *x, int *y)
{
  *x = offset_x[i];
  *y = offset_y[i];
}
