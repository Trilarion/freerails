#include "GuiEngine.h"
#include "Station.h"
#include "GameElement.h"

GuiEngine::GuiEngine(Player* _player,int w, int h){
  
  engine=new Engine(_player, w, h);
  initialize(_player);

}


GuiEngine::GuiEngine(Player* _player, int w, int h, int port){
  
  engine=new Engine(_player, w, h, port);
  initialize(_player);
  
}


GuiEngine::GuiEngine(Player* _player, int w, int h, char *server, int port){
  
  engine=new Engine(_player, w, h,server, port);
  initialize(_player);

}

GuiEngine::~GuiEngine(){

  /*  delete trackPositions; */
  
}


void GuiEngine::initialize(Player *_player){
  
  player=_player;
  stationController=(StationController *)engine->getControllerDispatcher()->getController(GameElement::idStation);
  trackController=(TrackController *)engine->getControllerDispatcher()->getController(GameElement::idTrack);
    
}


bool GuiEngine::testBuildStation(int x, int y){
    
  GameElement* new_element = new Station(x,y,NULL,"",Station::Small,NULL,NULL);
  return stationController->canBuildElement(new_element);

}

bool GuiEngine::buildStation(int x, int y){
    
  GameElement* new_element = new Station(x,y,NULL,"",Station::Small,NULL,NULL);
  if (stationController -> canBuildElement(new_element))
    {
      Message *msg=new Message(Message::addElement,1,(void *)new_element,player);
      sendMsg(msg);
      return true;
    }else
      return false;
  
}


bool GuiEngine::testBuildTrack(int x, int y, int dir){
    
  GameElement* new_element = new Track(x,y,NULL,dir);
  return trackController->canBuildElement(new_element);

}


bool GuiEngine::buildTrack(int x, int y, int dir){
    
  GameElement* new_element = new Track(x,y,NULL,dir);
  if (trackController->canBuildElement(new_element))
    {
      Message *msg=new Message(Message::addElement,1,(void *)new_element,player);
      sendMsg(msg);
      return true;
    }else
      return false;
    
}



void GuiEngine::changeGameState(GuiEngine::GameState _state){
  
  /* creates an object of type Engine::GameState */
  Engine::GameState *state=new Engine::GameState;
  *state=(Engine::GameState)_state;
  /* Creates the Message */
  Message* msg=new Message(Message::stateOfGame, 0,state);
  /* Sends the message down to the Engine*/
  sendMsg(msg);
  
}
