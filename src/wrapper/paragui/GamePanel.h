/*
 * $Id$
 */

#ifndef __GAMEPANEL_H__
#define __GAMEPANEL_H__

#include "GameMainWindow.h"
#include "GameMapView.h"

#include <pgthemewidget.h>
#include <pgradiobutton.h>
#include <pgbutton.h>
#include <pgrect.h>

#include <pgeventobject.h>

#include "GuiEngine.h"
#include "Message.h"

class GamePanel: public PG_ThemeWidget, public PG_EventObject {

  public:
    /**  */
    GamePanel(GameMainWindow* parent, int x, int y, int w, int h, GuiEngine* _engine, GameMapView* _mapView);
    /**  */
    ~GamePanel();

  private:
  
    PARAGUI_CALLBACK(pause_handler);
    PARAGUI_CALLBACK(clickTrackButton);
    PARAGUI_CALLBACK(clickStationButton);
    PARAGUI_CALLBACK(clickStationSelect);
    
    void releaseAllButtons(PG_Button* button);

    PG_Button* trackButton;
    PG_Button* stationButton;
    PG_Button* pauseButton;
    
    PG_RadioButton* stationSignal;
    PG_RadioButton* stationSmall;
    PG_RadioButton* stationMedium;
    PG_RadioButton* stationBig;
    
    
    GuiEngine* guiEngine;
    GameMapView* mapView;
};

#endif
