/*
 * $Id$
 */

#include "GamePanel.h"

/*PARAGUI_CALLBACK(GamePanel::pause_handler) {

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

PARAGUI_CALLBACK(GamePanel::quit_handler) {
std::cerr << "call quit" << std::endl;
  my_parent->getApp()->Quit();
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
        my_parent->getWidget()->setMouseType(GameMapView::buildTrack);
      break;
      case GamePanel::BuildStation:
        my_parent->getWidget()->setMouseType(GameMapView::buildStation);
      break;
      case GamePanel::BuildTrain:
        my_parent->getWidget()->setMouseType(GameMapView::buildTrain);
      break;
    }
    Update();
  } else {
    my_parent->getWidget()->setMouseType(GameMapView::normal);
  }
}

PARAGUI_CALLBACK(GamePanel::clickStationSelect) {

  switch (id)
  {
    case 4:
      my_parent->getWidget()->setStationType(Station::Signal);
    break;
    case 5:
      my_parent->getWidget()->setStationType(Station::Small);
    break;
    case 6:
      my_parent->getWidget()->setStationType(Station::Medium);
    break;
    case 7:
      my_parent->getWidget()->setStationType(Station::Big);
    break;
  }
}*/


GamePanel::GamePanel(GameMainWindow* parent, int x, int y, int w, int h, GuiEngine* _guiEngine):
PG_ThemeWidget(parent->getWidget(), PG_Rect(x,y,w,h), "Widget") {
  my_parent = parent;
  guiEngine=_guiEngine;

  SetBackgroundBlend(255);
  SetTransparency(128);
  stationViewButton=new PG_Button(this, /*GamePanel::ViewStations,*/PG_Rect(2,200,79,20),"Stations");
  stationViewButton->SetToggle(true);
  stationViewButton->SetPressed(true);
//  stationViewButton->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickViewButton);

  trainViewButton=new PG_Button(this, /*GamePanel::ViewTrains,*/PG_Rect(82,200,68,20),"Trains");
  trainViewButton->SetToggle(true);
//  trainViewButton->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickViewButton);
  
  trainList=new PG_WidgetList(this, PG_Rect(2,225,146, 170));
  trainList->EnableScrollBar(true, PG_ScrollBar::VERTICAL);
  trainList->SetTransparency(128);
  trainList->SetBackgroundBlend(255);
  trainList->SetVisible(false);
  trainList->SetDirtyUpdate(false);
  trainListSize=0;

  stationList=new PG_WidgetList(this, PG_Rect(2,225,146, 170));
  stationList->EnableScrollBar(true, PG_ScrollBar::VERTICAL);
  stationList->SetTransparency(128);
  stationList->SetBackgroundBlend(255);
  stationList->SetDirtyUpdate(false);
  stationListSize=0;
  
  trackButton=new PG_Button(this, /*GamePanel::BuildTrack,*/PG_Rect(5,400,25,25));
  trackButton->SetIcon("graphics/ui/buttons/build_track.png",
			"graphics/ui/buttons/build_track.png");
  trackButton->SetToggle(true);
//  trackButton->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickBuildButton);

  stationButton=new PG_Button(this, /*GamePanel::BuildStation,*/PG_Rect(35,400,25,25));
  stationButton->SetIcon("graphics/ui/buttons/build_station.png",
			 "graphics/ui/buttons/build_station.png");
  stationButton->SetToggle(true);
//  stationButton->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickBuildButton);

  trainButton=new PG_Button(this, /*GamePanel::BuildTrain,*/PG_Rect(65,400,25,25));
  trainButton->SetIcon("graphics/ui/buttons/build_train.png",
		       "graphics/ui/buttons/build_train.png");
  trainButton->SetToggle(true);
//  trainButton->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickBuildButton);
  
  stationSignal=new PG_RadioButton(this, PG_Rect(5, 430, 150, 20), "Signal Tower");
  stationSmall=new PG_RadioButton(this, PG_Rect(5, 450, 150, 20), "Depot", stationSignal);
  stationMedium=new PG_RadioButton(this, PG_Rect(5, 470, 150, 20), "Station", stationSignal);
  stationBig=new PG_RadioButton(this, PG_Rect(5, 490, 150, 20), "Terminal", stationSignal);

//  stationSignal->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickStationSelect);
//  stationSmall->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickStationSelect);
//  stationMedium->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickStationSelect);
//  stationBig->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::clickStationSelect);

  pauseButton=new PG_Button(this, PG_Rect(5,510,125,25),"PAUSE");
  pauseButton->SetToggle(true);
//  pauseButton->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::pause_handler);

  quitButton=new PG_Button(this, PG_Rect(5,540,125,25),"QUIT",PG_Button::CANCEL);
//  quitButton->SetEventObject(MSG_BUTTONCLICK, this, (MSG_CALLBACK_OBJ)&GamePanel::quit_handler);
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
  if (button!=trainButton) trainButton->SetPressed(false);
}

void GamePanel::releaseAllViewButtons(PG_Button* button) {
  if (button!=stationViewButton) stationViewButton->SetPressed(false);
  if (button!=trainViewButton) trainViewButton->SetPressed(false);
}

void GamePanel::addStation(Station* station)
{
  PG_Point point;
  PG_Image* image;
  PG_Button* button;
  point.x=0; point.y=0;
  switch(station->getSize())
  {
    case Station::Signal:
      return;
    break;
    case Station::Small:
    case Station::Medium:
    case Station::Big:
      button=new PG_Button(NULL, PG_Rect(0,0,150,30),station->getName().c_str());
      button->SetFontSize(11);
      button->SetTransparency(128,128,240);
    break;
  }
  stationList->AddChild(button);
  image=new PG_Image(button,point,"graphics/ui/buttons/build_station.png");
  image->SetVisible(true);
  stationList->Update();
  stationListSize++;
}

void GamePanel::addTrain(Train* train)
{
  PG_Point point;
  PG_Image* image;
  PG_Button* button;
  point.x=0; point.y=0;
/*  switch(train->getType())
  {
    case Train::coal:
    case Train::oil:
    case Train::elektro:
    case Train::magnet:
      button=new PG_Button(NULL, 0, PG_Rect(0,0,150,30),station->getName().c_str());
      button->SetFontSize(11);
    break;
  }
*/
  button=new PG_Button(NULL, PG_Rect(0,0,150,30),"");
  button->SetFontSize(11);
  button->SetTransparency(128,128,240);
  trainList->AddChild(button);
  image=new PG_Image(button,point,"graphics/ui/buttons/build_train.png");
  image->SetVisible(true);
  trainList->Update();
  trainListSize++;
}
