/*
 * $Id$
 */

#ifndef __GAMEMAPVIEW_H__
#define __GAMEMAPVIEW_H__

#include "GameMainWindow.h"
#include "WorldMap.h"
#include "GuiEngine.h"
#include "MapField.h"
#include "Base2DMapView.h"

#include <paragui_types.h>
#include <pgimage.h>
#include <pglabel.h>
#include <pgrect.h>
#include <pgscrollbar.h>
#include <pgthemewidget.h>

class GameMapView: public PG_ThemeWidget, Base2DMapView {

  public:
  
    enum MouseType {normal=0,
                    buildTrack=10, buildStation, buildTrain};
    /**  */
    GameMapView(GameMainWindow* parent, int x, int y, int w, int h, GuiEngine* _guiEngine);
    /**  */
    ~GameMapView();
    
    void setMouseType(MouseType type);
    void setStationType(Station::Size type);

  private:
    SDL_Surface* tilesImage;
    SDL_Surface* trackImage;
    SDL_Surface* trainImage;
    
    PG_ScrollBar* verticalScrollBar;
    PG_ScrollBar* horizontalScrollBar;
    
    SDL_Surface* imageSurface;
    PG_Image* view;
    PG_Widget* blind;
    MouseType mouseType;
    Station::Size stationType;

    int mouseOldMapX;
    int mouseOldMapY;
    
    PG_Point viewPos;
    
    void drawMapPixmap(int mapX, int mapY);
    void drawElementsPixmap(int mapX, int mapY);
    void drawTrackPixmap(int mapX, int mapY);

    void drawStationPixmap(int mapX, int mapY, Station* station);
    void drawTrackPixmap(int mapX, int mapY, Track* track);
    void drawTrainPixmap(int mapX, int mapY);
    void drawPixmap(SDL_Surface* pixmap, int tilesetX, int tilesetY, int mapX, int mapY);
    void drawTilesPixmap(int tilesetPosX, int tilesetPosY, int mapX, int mapY);
    
    void regenerateTile(int x, int y); // x and y are the position of the tile on which the mouse is now.
    void showTrack(int x, int y, unsigned int dir);

    void eventMouseLeave();
    bool eventMouseButtonDown(const SDL_MouseButtonEvent* button);
    bool eventMouseMotion(const SDL_MouseMotionEvent* motion);

    bool eventScrollPos(int id, PG_Widget* widget,unsigned long data);
    bool eventScrollTrack(int id, PG_Widget* widget,unsigned long data);
    
    void moveXto(unsigned long data);
    void moveYto(unsigned long data);
    
    void writeCities();
    void redrawMap(int x, int y, int w, int h);

};

#endif
