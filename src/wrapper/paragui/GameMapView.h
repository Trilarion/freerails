/*
 * $Id$
 */

#ifndef __GAMEMAPVIEW_H__
#define __GAMEMAPVIEW_H__

#include "GameMainWindow.h"
#include "WorldMap.h"
#include "MapField.h"

#include <paragui_types.h>
#include <pggradientwidget.h>
#include <pgrect.h>
#include <pgimage.h>

class GameMapView: public PG_GradientWidget {

  public:
    /**  */
    GameMapView(GameMainWindow* parent, int x, int y, int w, int h, WorldMap* _worldMap);
    /**  */
    ~GameMapView();

  private:
    WorldMap* worldMap;
    std::vector<PG_Image *> imageField;
    
    SDL_Surface* sdlimage;
    
    SDL_Surface* getMapImage(int x, int y);
    int getImagePos(int x, int y, MapField::FieldType type);
    int getRiverImagePos(int x, int y);
    int get3DImagePos(int x, int y, MapField::FieldType type);

};

#endif