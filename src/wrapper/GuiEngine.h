/*
 * $Id$
 */

#ifndef __GUIENGINE_H__
#define __GUIENGINE_H__

#include "WorldMap.h"
#include "Engine.h"
#include "MapField.h"
#include "GameElement.h"
#include "Track.h"
#include "TrackController.h"
#include "Station.h"
#include "StationController.h"
#include "City.h"
#include "CityController.h"
#include "Message.h"

#include <map>
#include <vector>
#include <string>
#include <math.h>



class GuiEngine{

 public:
  
  enum GameState { ask=0, Initializing, Waiting, Starting, Running, Pausing, Stopping, Error };
    
  GuiEngine(Player* _player, int w, int h); // For a single game
  GuiEngine(Player* _player, int w, int h, int port); // For a Server game
  GuiEngine(Player* _player, int w, int h, char *server, int port); // For a Client game
  ~GuiEngine();
  
  inline Player* getPlayer(){return player;};

  inline void checkNet(){engine->checkNet();};
  inline void checkNext(int msec){engine->checkNext(msec);}; // This is the function wich will called from ParaGUI or Qt by there timer

  bool haveMsg();
  Message* getMsg();

  
  inline WorldMap* getWorldMap() {return engine->getWorldMap();};  
  inline GameState getGameState(){return ((GameState)engine->getGameState());};
  
  void changeGameState(GuiEngine::GameState state);
  

  bool testBuildStation(int x, int y);
  bool buildStation(int x, int y, Station::Size size);
  
  bool testBuildTrack(int x, int y, int dir);
  bool buildTrack(int x, int y, int dir);

  bool testBuildTrain(int x, int y);
  bool buildTrain(int x, int y);
  
  void getOtherConnectionSide(unsigned int* x, unsigned int* y, int* dir) {trackController->getOtherConnectionSide(x,y,dir);}
  City* getNearestCity(int x, int y);
  
  std::vector<City*> getAllCities();
  
 private:
  
  Engine *engine;
  TrackController* trackController;
  StationController* stationController;
  TrainController* trainController;
  Player *player;
  
  std::vector<std::string> nameExtensions;

  void initialize(Player *_player);
  std::string getStationName(std::string cityName);
  bool stationNameExists(std::string _name);
  inline void sendMsg(Message *msg){engine->sendMsg(msg);};

};





#endif /* __GUIENGINE_H__ */
