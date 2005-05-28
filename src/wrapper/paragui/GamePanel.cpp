/*
 * $Id$
 */

#include "GamePanel.h"

/*
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


GamePanel::GamePanel(GameMainWindow* parent, int x, int y, int w, int h, GuiEngine* _guiEngine, MapHelper* mapHelper):
PG_ThemeWidget(parent->getWidget(), PG_Rect(x,y,w,h), "Widget") {
  my_parent = parent;
  guiEngine=_guiEngine;

  SetBackgroundBlend(255);
  SetTransparency(128);
  terrainViewButton=new PG_Button(this, PG_Rect(2,190,40,35));
  terrainViewButton->SetIcon("graphics/icons/terrain_info.png");
  terrainViewButton->SetToggle(true);
  terrainViewButton->SetPressed(true);
  terrainViewButton->sigClick.connect(slot(*this, &GamePanel::handleViewButtonClick));

  buildViewButton=new PG_Button(this, PG_Rect(42,190,40,35));
  buildViewButton->SetIcon("graphics/icons/track_new.png");
  buildViewButton->SetToggle(true);
  buildViewButton->sigClick.connect(slot(*this, &GamePanel::handleViewButtonClick));

  trainViewButton=new PG_Button(this, PG_Rect(82,190,40,35));
  trainViewButton->SetIcon("graphics/icons/train_list.png");
  trainViewButton->SetToggle(true);
  trainViewButton->sigClick.connect(slot(*this, &GamePanel::handleViewButtonClick));

  stationViewButton=new PG_Button(this, PG_Rect(122,190,40,35));
  stationViewButton->SetIcon("graphics/icons/station_info.png");
  stationViewButton->SetToggle(true);
  stationViewButton->sigClick.connect(slot(*this, &GamePanel::handleViewButtonClick));

  trainList=new PG_WidgetList(this, PG_Rect(2, 225, 161, 200));
  trainList->EnableScrollBar(true, PG_ScrollBar::VERTICAL);
  trainList->SetTransparency(128);
  trainList->SetBackgroundBlend(255);
  trainList->SetHidden(true);
  trainList->SetVisible(true);
  trainList->SetDirtyUpdate(false);
  trainListSize=0;

  stationList=new PG_WidgetList(this, PG_Rect(2, 225, 161, 200));
  stationList->EnableScrollBar(true, PG_ScrollBar::VERTICAL);
  stationList->SetTransparency(128);
  stationList->SetBackgroundBlend(255);
  stationList->SetHidden(true);
  stationList->SetVisible(true);
  stationList->SetDirtyUpdate(false);
  stationListSize=0;
  
  infoPane=new TerrainInfoPane(this, 2, 225, 161, 200, guiEngine, mapHelper);
  infoPane->SetTransparency(128);
  infoPane->SetBackgroundBlend(255);
  infoPane->SetHidden(true);
  infoPane->SetVisible(true);

  buildPane=new TerrainBuildPane(this, 2, 225, 161, 200, guiEngine, parent->getWidget());
  buildPane->SetTransparency(128);
  buildPane->SetBackgroundBlend(255);
  buildPane->SetHidden(true);
  buildPane->SetVisible(true);
  
  parent->getWidget()->setInfoPane(infoPane);
  
  pauseButton=new PG_Button(this, PG_Rect(5,y+h-30,75,25),"PAUSE");
  pauseButton->SetToggle(true);
  pauseButton->sigClick.connect(slot(*this, &GamePanel::handleGameButtonClick));

  quitButton=new PG_Button(this, PG_Rect(85,y+h-30,75,25),"QUIT",PG_Button::CANCEL);
  quitButton->sigClick.connect(slot(*this, &GamePanel::handleGameButtonClick));
  
  infoPane->Show();
}

GamePanel::~GamePanel() {
}

void GamePanel::releaseAllBuildButtons(PG_Button* button) {
/*  if (button!=buildButton) buildButton->SetPressed(false);
  if (button!=upgradeButton) upgradeButton->SetPressed(false);
  if (button!=stationButton) stationButton->SetPressed(false);
  if (button!=removeButton) removeButton->SetPressed(false);*/
}

void GamePanel::releaseAllViewButtons(PG_Button* button) {
  if (button!=terrainViewButton) terrainViewButton->SetPressed(false);
  if (button!=buildViewButton) buildViewButton->SetPressed(false);
  if (button!=trainViewButton) trainViewButton->SetPressed(false);
  if (button!=stationViewButton) stationViewButton->SetPressed(false);
}

void GamePanel::releaseAllViews() {
  infoPane->Hide();
  trainList->Hide();
  stationList->Hide();
  buildPane->Hide();
}

bool GamePanel::handleViewButtonClick(PG_Button* button) {

  releaseAllViewButtons(button);
  releaseAllViews();
  if (!button->GetPressed()) return true;
  if (button==terrainViewButton)
  {
    buildPane->Hide();
    trainList->Hide();
    stationList->Hide();
    infoPane->Show();
  } else if (button==buildViewButton)
  {
    infoPane->Hide();
    trainList->Hide();
    stationList->Hide();
    buildPane->Show();
  } else if (button==trainViewButton)
  {
    infoPane->Hide();
    buildPane->Hide();
    trainList->Hide();
    stationList->Show();
  } else if (button==stationViewButton)
  {
    infoPane->Hide();
    buildPane->Hide();
    stationList->Hide();
    trainList->Show();
  }
  return true;
}


bool GamePanel::handleGameButtonClick(PG_Button* button) {

  if (button==pauseButton)
  {
    switch (guiEngine->getGameState())
    {
      case GuiEngine::Pausing:
        guiEngine->changeGameState(GuiEngine::Running);
      break;
      case GuiEngine::Running:
        guiEngine->changeGameState(GuiEngine::Pausing);
      break;
      default:
        std::cerr << "false" << std::endl;
        return false;
    }
  } else if (button==quitButton)
  {
    my_parent->getApp()->Quit();
  }
  return true;
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
