/*
 * $Id$
 */

#ifndef __GAMEPANEL_H__
#define __GAMEPANEL_H__

#include "GameMainWindow.h"
#include "GameMapView.h"

#include <pgthemewidget.h>
#include <pgbutton.h>
#include <pgrect.h>

#include <pgeventobject.h>

#include "Engine.h"
#include "Message.h"

class GamePanel: public PG_ThemeWidget, public PG_EventObject {

  public:
    /**  */
    GamePanel(GameMainWindow* parent, int x, int y, int w, int h, Engine* _engine, GameMapView* _mapView);
    /**  */
    ~GamePanel();

  private:
  
    PARAGUI_CALLBACK(pause_handler);
    PARAGUI_CALLBACK(clickTrackButton);
    PARAGUI_CALLBACK(clickStationButton);
    
    void releaseAllButtons(PG_Button* button);

    PG_Button* trackButton;
    PG_Button* stationButton;
    PG_Button* pauseButton;
    Engine* engine;
    GameMapView* mapView;
};

#endif