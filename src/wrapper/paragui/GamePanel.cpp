/*
 * $Id$
 */

#include "GamePanel.h"

PARAGUI_CALLBACK(GamePanel::pause_handler) {

  Message* msg=new Message(Message::pauseGame,NULL);
  engine->sendMsg(msg);
  
}

GamePanel::GamePanel(GameMainWindow* parent, int x, int y, int w, int h, Engine* _engine):
PG_GradientWidget(parent->getWidget(), PG_Rect(x,y,w,h), "GradientWidget") {
  SetBackgroundBlend(0);
  trackButton=new PG_Button(this,1,PG_Rect(5,400,25,25));
  trackButton->SetIcon("graphics/ui/buttons/build_track_up.png",
			"graphics/ui/buttons/build_track_down.png");
  stationButton=new PG_Button(this,2,PG_Rect(35,400,25,25));
  stationButton->SetIcon("graphics/ui/buttons/build_station_up.png",
			 "graphics/ui/buttons/build_station_down.png");
  pauseButton=new PG_Button(this,3,PG_Rect(5,430,125,25),"PAUSE");
  pauseButton->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::pause_handler);
  engine=_engine;
}

GamePanel::~GamePanel() {
  delete trackButton;
  delete stationButton;
}