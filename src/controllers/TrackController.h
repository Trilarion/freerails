/***************************************************************************
                          trackcontroller.h  -  description
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

#ifndef TRACKCONTROLLER_H
#define TRACKCONTROLLER_H

#include "Controller.h"
#include "Track.h"
#include "WorldMap.h"

/**This class is handling build and remove of tracks.
  *@author frank
  */

class GameElement;
class Serializer;
class WorldMap;

class TrackController : public Controller
{
  enum TrackType { Normal = 0, Bridge };
  
  public: 
    TrackController(WorldMap *_map);
    ~TrackController();

    GameElement* CreateElement(Serializer* _serializer);
    bool canBuildElement(GameElement* _element);
    void addGameElement(GameElement* _element);
    void removeGameElement(long int _elementID);

    void getOtherConnectionSide(unsigned int* x, unsigned int* y, int* dir);

  private:
    bool testBuildElement(int x, int y, int dir);
    bool connectIsBuildable(unsigned int connect);
    unsigned int doConnect(unsigned int connect, int dir);
    void trackUpdate(int x, int y, int dir);
    void trackDoBuild(int x, int y, int dir);
    
//    void setCorner(int x, int y);
};

#endif
