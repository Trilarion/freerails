/*
 * $Id$
 */

#include "GameNetHandler.h"

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
        panel->addStation((Station*)element);
      break;
      case GameElement::idTrain:
        panel->addTrain((Train*)element);
//	framehandler->AddFrameObject(new TrainFrame(guiEngine->getWorldMap(),(Train*)element));
      break;
    case GameElement::idTrack:
std::cerr << "Add Track" << std::endl;
	mw->getApp()->GetFrameHandler()->UpdateBackground(((Train*)element)->getPosX(),((Train*)element)->getPosY());
	break;
    }
  }
}
