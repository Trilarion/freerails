/*
 * $Id$
 */

#ifndef __GAMEMAPVIEW_H__
#define __GAMEMAPVIEW_H__

#include "GameMainWindow.h"
#include "WorldMap.h"

#include <pggradientwidget.h>
#include <pgrect.h>

class GameMapView: public PG_GradientWidget {

  public:
    /**  */
    GameMapView(GameMainWindow* parent, int x, int y, int w, int h, WorldMap* _worldMap);
    /**  */
    ~GameMapView();

  private:
    WorldMap* worldMap;

};

#endif