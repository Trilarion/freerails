/*
 * $Id$
 */

#include "GamePanel.h"

GamePanel::GamePanel(GameMainWindow* parent, int x, int y, int w, int h):
PG_GradientWidget(parent->getWidget(), PG_Rect(x,y,w,h), "GradientWidget") {
  SetBackgroundBlend(0);
  trackButton=new PG_Button(this,1,PG_Rect(5,400,25,25));
  trackButton->SetIcon("graphics/ui/buttons/build_track_up.png",
			"graphics/ui/buttons/build_track_down.png");
  stationButton=new PG_Button(this,2,PG_Rect(35,400,25,25));
  stationButton->SetIcon("graphics/ui/buttons/build_station_up.png",
			 "graphics/ui/buttons/build_station_down.png");
}

GamePanel::~GamePanel() {
  delete trackButton;
  delete stationButton;
}