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
#include <pgwidgetlist.h>

#include <pgeventobject.h>

#include "GuiEngine.h"
#include "Message.h"
#include "Station.h"
#include "Train.h"

class GamePanel: public PG_ThemeWidget, public PG_EventObject {

  public:
    enum WidgetID {ViewStations = 10000, ViewTrains, BuildTrack, BuildStation, BuildTrain };
    /**  */
    GamePanel(GameMainWindow* parent, int x, int y, int w, int h, GuiEngine* _engine, GameMapView* _mapView);
    /**  */
    ~GamePanel();
    
    void addStation(Station* station);
    void addTrain(Train* train);

  private:
  
    PARAGUI_CALLBACK(pause_handler);
    PARAGUI_CALLBACK(clickViewButton);
    PARAGUI_CALLBACK(clickBuildButton);
    PARAGUI_CALLBACK(clickStationSelect);
    
    void releaseAllViewButtons(PG_Button* button);
    void releaseAllBuildButtons(PG_Button* button);

    PG_Button* stationViewButton;
    PG_Button* trainViewButton;
    
    PG_WidgetList* stationList;
    PG_WidgetList* trainList;
    
    int stationListSize;
    int trainListSize;

    PG_Button* trackButton;
    PG_Button* stationButton;
    PG_Button* trainButton;
    PG_Button* pauseButton;
    
    PG_RadioButton* stationSignal;
    PG_RadioButton* stationSmall;
    PG_RadioButton* stationMedium;
    PG_RadioButton* stationBig;
    
    
    GuiEngine* guiEngine;
    GameMapView* mapView;
};

#endif
