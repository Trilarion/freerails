/*
 * $Id$
 */

#include "GameNetHandler.h"
#include "TrainFrame.h"

GameNetHandler::GameNetHandler(GameMainWindow* _mw, GuiEngine* _guiEngine, GamePanel* _panel, GameMapView *_mapView):
PG_NetHandler(NULL) {
  guiEngine = _guiEngine;
  panel = _panel;
  mw = _mw;
  mapView = _mapView;
}

GameNetHandler::~GameNetHandler() {
}

void GameNetHandler::checkNet() {

  if (guiEngine->haveMsg())
  {
    Message* msg = guiEngine->getMsg();
    switch (msg->getMsgType())
    {
      case Message::addElement:
        addings((GameElement*)msg->getData());
      break;
    }
  }
  return;
}

void GameNetHandler::addings(GameElement* element)
{
  if(element!=NULL)
  {
    switch(element->getTypeID())
    {
      case GameElement::idStation:
std::cerr << "Add Station" << std::endl;
        panel->addStation((Station*)element);
	mw->getApp()->GetFrameHandler()->UpdateTiles(((Station*)element)->getPosX(),((Station*)element)->getPosY());
      break;
      case GameElement::idTrain:
std::cerr << "Add Train start" << std::endl;
        panel->addTrain((Train*)element);
	mw->getApp()->GetFrameHandler()->AddFrameObject(new TrainFrame(guiEngine->getWorldMap(),(Train*)element));
std::cerr << "Add Train done" << std::endl;
      break;
    case GameElement::idTrack:
std::cerr << "Add Track start" << std::endl;
	mw->getApp()->GetFrameHandler()->UpdateTiles(((Track*)element)->getPosX(),((Track*)element)->getPosY());
std::cerr << "Add Track end" << std::endl;
	break;
    }
  }
}
