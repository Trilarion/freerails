/*
 * $Id$
 */

#include "GamePanel.h"

PARAGUI_CALLBACK(GamePanel::pause_handler) {

  switch (guiEngine->getGameState())
  {
    case GuiEngine::Pausing:
      guiEngine->changeGameState(GuiEngine::Running);
      pauseButton->SetPressed(false);
    break;
    case GuiEngine::Running:
      guiEngine->changeGameState(GuiEngine::Pausing);
      pauseButton->SetPressed(true);
    break;
    default:
    break;
  }
}

PARAGUI_CALLBACK(GamePanel::clickViewButton) {

  PG_Button* button = (PG_Button*)widget;
  releaseAllViewButtons(button);
  switch (id)
  {
    case GamePanel::ViewStations:
      trainList->SetVisible(false);
      stationList->SetVisible(true);
    break;
    case GamePanel::ViewTrains:
      stationList->SetVisible(false);
      trainList->SetVisible(true);
    break;
  }
  button->SetPressed(true);
  Update();
}

PARAGUI_CALLBACK(GamePanel::clickBuildButton) {

  PG_Button* button = (PG_Button*)widget;
  if (button->GetPressed()) {
    releaseAllBuildButtons(button);
    switch (id)
    {
      case GamePanel::BuildTrack:
        mapView->setMouseType(GameMapView::buildTrack);
      break;
      case GamePanel::BuildStation:
        mapView->setMouseType(GameMapView::buildStation);
      break;
    }
    Update();
  } else {
    mapView->setMouseType(GameMapView::normal);
  }
}

PARAGUI_CALLBACK(GamePanel::clickStationSelect) {

  switch (id)
  {
    case 4:
      mapView->setStationType(Station::Signal);
    break;
    case 5:
      mapView->setStationType(Station::Small);
    break;
    case 6:
      mapView->setStationType(Station::Medium);
    break;
    case 7:
      mapView->setStationType(Station::Big);
    break;
  }
}


GamePanel::GamePanel(GameMainWindow* parent, int x, int y, int w, int h, GuiEngine* _guiEngine, GameMapView* _mapView):
PG_ThemeWidget(parent->getWidget(), PG_Rect(x,y,w,h), "ThemeWidget") {
  SetBackgroundBlend(0);
  stationViewButton=new PG_Button(this,GamePanel::ViewStations,PG_Rect(2,200,79,20),"Stations");
  stationViewButton->SetToggle(true);
  stationViewButton->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickViewButton);

  trainViewButton=new PG_Button(this,GamePanel::ViewTrains,PG_Rect(82,200,68,20),"Trains");
  trainViewButton->SetToggle(true);
  trainViewButton->SetPressed(true);
  trainViewButton->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickViewButton);
  
  stationList=new PG_WidgetList(this, PG_Rect(2,225,146, 170));
  stationList->EnableScrollBar(true, PG_SB_VERTICAL);
  stationList->SetTransparency(0);
  
  new PG_Label(stationList, PG_Rect(0,0,300,20), "Stations");

  trainList=new PG_WidgetList(this, PG_Rect(2,225,146, 170));
  trainList->EnableScrollBar(true, PG_SB_VERTICAL);
  trainList->SetTransparency(0);
  trainList->SetVisible(false);

  new PG_Label(trainList, PG_Rect(0,0,100,20), "Trains");

  trackButton=new PG_Button(this,GamePanel::BuildTrack,PG_Rect(5,400,25,25));
  trackButton->SetIcon("graphics/ui/buttons/build_track_up.png",
			"graphics/ui/buttons/build_track_down.png");
  trackButton->SetToggle(true);
  trackButton->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickBuildButton);

  stationButton=new PG_Button(this,GamePanel::BuildStation,PG_Rect(35,400,25,25));
  stationButton->SetIcon("graphics/ui/buttons/build_station_up.png",
			 "graphics/ui/buttons/build_station_down.png");
  stationButton->SetToggle(true);
  stationButton->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickBuildButton);
  
  stationSignal=new PG_RadioButton(this, 4, PG_Rect(5, 430, 150, 20), "Signal Tower");
  stationSmall=new PG_RadioButton(this, 5, PG_Rect(5, 450, 150, 20), "Depot", stationSignal);
  stationMedium=new PG_RadioButton(this, 6, PG_Rect(5, 470, 150, 20), "Station", stationSignal);
  stationBig=new PG_RadioButton(this, 7, PG_Rect(5, 490, 150, 20), "Terminal", stationSignal);

  stationSignal->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickStationSelect);
  stationSmall->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickStationSelect);
  stationMedium->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickStationSelect);
  stationBig->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickStationSelect);

  pauseButton=new PG_Button(this,3,PG_Rect(5,510,125,25),"PAUSE");
  pauseButton->SetToggle(true);
  pauseButton->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::pause_handler);
  
  guiEngine=_guiEngine;
  mapView=_mapView;
}

GamePanel::~GamePanel() {
  delete trackButton;
  delete stationButton;
  delete pauseButton;
  delete stationSignal;
  delete stationSmall;
  delete stationMedium;
  delete stationBig;
}

void GamePanel::releaseAllBuildButtons(PG_Button* button) {
  if (button!=trackButton) trackButton->SetPressed(false);
  if (button!=stationButton) stationButton->SetPressed(false);
}

void GamePanel::releaseAllViewButtons(PG_Button* button) {
  if (button!=stationViewButton) stationViewButton->SetPressed(false);
  if (button!=trainViewButton) trainViewButton->SetPressed(false);
}
