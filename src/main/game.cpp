#include <clocale>
#include <iostream>

#include "game.h"

#include "Player.h"
#include "GameElement.h"
#include "Train.h"

#include "WorldMap.h"
#include "MapGenerator.h"

#include "FreeRailsLog.h"

MyGameApplication::MyGameApplication(int argc, char *argv[]) : GameApplication(argc, argv)
{
  /*  worldMap=NULL; */
  // Some rather silly demonstration code:
/*  GameController controll("default",1900,1,1);
  Train train(&controll,NULL, &pl);
  train.setPlayer(&pl);
  pl.addGameElement(&train);
*/
  FreeRailsLog("game: STARTING FREERAILS");  

}

MyGameApplication::~MyGameApplication()
{

  FreeRailsLog logFile("game: ENDING FREERAILS");  
  logFile.close();

}

void MyGameApplication::initSingleGame(const std::string name, int playFieldWidth, int playFieldHeight, int /* numberOfAi */)
{
  std::cerr << "SingleGame" << std::endl;
  playerSelf = new Player(name, Player::HUMAN);

  if(playFieldWidth == -1)
  {
    // we will play a scenario
  }
  else
  {
    
    /* worldMap = MapGenerator().generateWorld(playFieldWidth, playFieldHeight); */
  }
}

void MyGameApplication::initServerGame(const std::string name, int playFieldWidth, int playFieldHeight, int /*numberOfAi*/)
{
  std::cerr << "ServerGame" << std::endl;
  playerSelf = new Player(name,Player::HUMAN);
  if(playFieldWidth == -1)
  {
    // we will play a scenario
  }
  else
  {
    /*  worldMap = MapGenerator().generateWorld(playFieldWidth, playFieldHeight);  */
  }
}

void MyGameApplication::initClientGame(const std::string name)
{
  std::cerr << "ClientGame" << std::endl;
  playerSelf = new Player(name,Player::HUMAN);

}
