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

//#include <paragui_types.h>
#include <pgimage.h>
#include <pglabel.h>
#include <pgrect.h>
#include <pgscrollbar.h>
#include <pgthemewidget.h>
#include <pgframeapplication.h>
#include <pgspritebase.h>
#include <pgspriteobject.h>


class GameMapView: public PG_Widget, Base2DMapView {

  public:
  
    enum MouseType {normal=0,
                    buildTrack=10, buildStation, buildTrain};
    /**  */
    GameMapView(PG_FrameApplication* _app, int x, int y, int w, int h);
    /**  */
    ~GameMapView();
    
    void setMouseType(MouseType type);
    void setStationType(Station::Size type);
    void setGuiEngine(GuiEngine* _guiEngine);

  private:
    MouseType mouseType;
    Station::Size stationType;
    void eventMouseLeave();
    void eventMouseEnter();
    bool eventMouseButtonDown(const SDL_MouseButtonEvent* button);
    bool eventMouseMotion(const SDL_MouseMotionEvent* motion);

    bool selected;
    int selectedX;
    int selectedY;
    PG_SpriteBase* selectedbase;
    PG_SpriteObject* selectedsprite;
	
    PG_Point viewPos;
    PG_FrameApplication* app;    

    void drawSelected(int mapX, int mapY);
    bool adjacentTile(int mapX, int mapY, int *dir);
};

#endif
