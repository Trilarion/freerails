#include "GuiEngine.h"

/* single game */
GuiEngine::GuiEngine(Player* _player,int w, int h){
  
  engine=new Engine(_player, w, h);
  initialize(_player);

}


/* server game */
GuiEngine::GuiEngine(Player* _player, int w, int h, int port){
  
  engine=new Engine(_player, w, h, port);
  initialize(_player);
  
}

/* client game */
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
  
  nameExtensions.push_back("Station");
  nameExtensions.push_back("Junction");
  nameExtensions.push_back("Siding");
  nameExtensions.push_back("North");
  nameExtensions.push_back("East");
  nameExtensions.push_back("South");
  nameExtensions.push_back("West");
}


bool GuiEngine::testBuildStation(int x, int y){
    
  GameElement* new_element = new Station(x,y,player,"",Station::Small,NULL);
  return stationController->canBuildElement(new_element);

}

bool GuiEngine::buildStation(int x, int y, Station::Size size){

  GameElement* new_element = new Station(x, y, player, getStationName(getNearestCity(x,y)->getName()).c_str(), size, NULL);
  if (stationController -> canBuildElement(new_element))
    {
      Message *msg=new Message(Message::addElement,1,(void *)new_element,player);
      sendMsg(msg);
      return true;
    }else
      return false;
  
}


bool GuiEngine::testBuildTrack(int x, int y, int dir){
    
  GameElement* new_element = new Track(x,y,player,dir);
  return trackController->canBuildElement(new_element);

}


bool GuiEngine::buildTrack(int x, int y, int dir){
    
  GameElement* new_element = new Track(x,y,player,dir);
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


bool GuiEngine::haveMsg(){

  return engine->haveMsg();

}


Message* GuiEngine::getMsg(){

  return engine->getMsg();

}

std::vector<City*> GuiEngine::getAllCities(){

  Controller* con=engine->getControllerDispatcher()->getController(GameElement::idCity);
  std::vector<City*> allCities;
  std::map<GameElement::ElementID, GameElement*> all=con->getGameElements();
  std::map<GameElement::ElementID, GameElement*>::iterator it;
  for (it = all.begin(); it != all.end(); ++it)
  {
    allCities.push_back((City*)(*it).second);
  }
  return allCities;

}

City* GuiEngine::getNearestCity(int x, int y) {

  City* city = NULL;
  double distance = -1;
  std::vector<City*> all = getAllCities();
  std::vector<City*>::iterator it;
  for (it = all.begin(); it != all.end(); ++it)
  {
    City* tmp = (City*)(*it);
    double a = (x - tmp->getPosX()) * (x - tmp->getPosX());
    double b = (y - tmp->getPosY()) * (y - tmp->getPosY());
    double tmpDist = sqrt(a+b);
    if (distance==-1 || tmpDist<distance)
    {
      city=tmp;
      distance=tmpDist;
    }
  }
  return city;
}

std::string GuiEngine::getStationName(std::string cityName) {

  std::vector<std::string>::iterator it;
  for (it = nameExtensions.begin(); it!=nameExtensions.end(); ++it)
  {
    if (!(stationNameExists(cityName + " " + (std::string)(*it))))
    {
      return cityName + " " + (std::string)(*it);
    }
  }
  return cityName + " TODO";
}

bool GuiEngine::stationNameExists(std::string _name)
{
  std::map<GameElement::ElementID, GameElement*> map = stationController->getGameElements();
  std::map<GameElement::ElementID, GameElement*>::iterator it;
  std::cerr << std::endl;
  for (it = map.begin(); it != map.end(); ++it)
  {
    if (((Station*)(*it).second)->getName()==_name) return true;
  }
  return false;
}
