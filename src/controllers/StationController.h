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
#include "Station.h"
#include "WorldMap.h"

#include <string>

/**This class is handling build and remove of stations.
  *@author frank
  */

class StationController : public Controller
{
  public: 
    StationController(WorldMap *_map);
    ~StationController();

    GameElement* CreateElement(Serializer* _serializer);
    bool canBuildElement(GameElement* _element);
    void addGameElement(GameElement* _element);
    void removeGameElement(long int _elementID);

  private:
    bool testBuildElement(int x, int y);
    bool connectIsBuildable(unsigned int connect);
    void stationDoBuild(Station* station);

};

#endif
