/*
 * $Id$
 */

#include "GamePanel.h"

PARAGUI_CALLBACK(GamePanel::pause_handler) {

  Engine::GameState state = Engine::Pausing;
  Message* msg=new Message(Message::stateOfGame,0,&state);
  engine->sendMsg(msg);
  
}

PARAGUI_CALLBACK(GamePanel::clickTrackButton) {

  if (trackButton->GetPressed()) {
    releaseAllButtons(trackButton);
    mapView->setMouseType(GameMapView::buildTrack);
    Update();
  } else {
    mapView->setMouseType(GameMapView::normal);
  }
}

PARAGUI_CALLBACK(GamePanel::clickStationButton) {

  if (trackButton->GetPressed()) {
    releaseAllButtons(stationButton);
    mapView->setMouseType(GameMapView::buildStation);
    Update();
  } else {
    mapView->setMouseType(GameMapView::normal);
  }
}


GamePanel::GamePanel(GameMainWindow* parent, int x, int y, int w, int h, Engine* _engine, GameMapView* _mapView):
PG_ThemeWidget(parent->getWidget(), PG_Rect(x,y,w,h), "ThemeWidget") {
  SetBackgroundBlend(0);
  trackButton=new PG_Button(this,1,PG_Rect(5,400,25,25));
  trackButton->SetIcon("graphics/ui/buttons/build_track_up.png",
			"graphics/ui/buttons/build_track_down.png");
  trackButton->SetToggle(true);
  trackButton->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickTrackButton);

  stationButton=new PG_Button(this,2,PG_Rect(35,400,25,25));
  stationButton->SetIcon("graphics/ui/buttons/build_station_up.png",
			 "graphics/ui/buttons/build_station_down.png");
  stationButton->SetToggle(true);
  stationButton->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickStationButton);

  pauseButton=new PG_Button(this,3,PG_Rect(5,430,125,25),"PAUSE");
  pauseButton->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::pause_handler);
  
  engine=_engine;
  mapView=_mapView;
}

GamePanel::~GamePanel() {
  delete trackButton;
  delete stationButton;
}

void GamePanel::releaseAllButtons(PG_Button* button) {
  if (button!=trackButton) trackButton->SetPressed(false);
  if (button!=stationButton) stationButton->SetPressed(false);
}