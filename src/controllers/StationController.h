/***************************************************************************
                          stationcontroller.h  -  description
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

#ifndef STATIONCONTROLLER_H
#define STATIONCONTROLLER_H

#include "Controller.h"
#include "Track.h"
#include "WorldMap.h"

/**This class is handling build and remove of stations.
  *@author frank
  */

class StationController : public Controller
{
  public: 
    StationController(WorldMap *_map);
    ~StationController();

    GameElement* CreateElement(Serializer* _serializer);
    bool canBuildElement(int x, int y, int d);
    void addGameElement(void *_data);
    void removeGameElement(void *_data);

  private:
    bool testBuildElement(int x, int y);
    bool connectIsBuildable(unsigned int connect);
    void stationDoBuild(int x, int y);

};

#endif
