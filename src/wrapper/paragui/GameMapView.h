/*
 * $Id$
 */

#ifndef __GAMEMAPVIEW_H__
#define __GAMEMAPVIEW_H__

#include "GameMainWindow.h"
#include "WorldMap.h"
#include "Engine.h"
#include "MapField.h"

#include <paragui_types.h>
#include <pgthemewidget.h>
#include <pgrect.h>
#include <pgimage.h>
#include <pgwidgetlist.h>

class GameMapView: public PG_ThemeWidget {

  public:
  
    enum MouseType {normal=0,
                    buildTrack=10, buildStation, buildSignal};
    /**  */
    GameMapView(GameMainWindow* parent, int x, int y, int w, int h, Engine* _engine);
    /**  */
    ~GameMapView();
    
    void setMouseType(MouseType type);

  private:
    Engine* engine;
    
    SDL_Surface* tilesImage;
    SDL_Surface* trackImage;
    SDL_Surface* imageSurface;
    PG_WidgetList* WidgetList;
    PG_Image* view;
    int mouseType;
    int mouseOldX;
    int mouseOldY;
    
    void getMapImage(SDL_Surface* surface,int x, int y);
    int getImagePos(int x, int y, MapField::FieldType type);
    int getRiverImagePos(int x, int y);
    int get3DImagePos(int x, int y, MapField::FieldType type);
    
    void regenerateTile(int x, int y); // x and y are the position of the tile on which the mouse is now.
    void showTrack(int x, int y,int tracktileX, int tracktileY);

    void eventMouseLeave();
    bool eventMouseButtonDown(const SDL_MouseButtonEvent* button);
    bool eventMouseMotion(const SDL_MouseMotionEvent* motion);

};

#endif
