
#include <iostream>

#include "game.h"

#include "Player.h"
#include "GameElement.h"
#include "Train.h"

#include "WorldMap.h"
#include "MapGenerator.h"

MyGameApplication::MyGameApplication(int argc, char *argv[]):GameApplication(argc, argv) {

  worldMap=NULL;
  // Some rather silly demonstration code:
/*  GameController controll("default",1900,1,1);
  Train train(&controll,NULL, &pl);
  train.setPlayer(&pl);
  pl.addGameElement(&train);
*/
}

MyGameApplication::~MyGameApplication() {

}

void MyGameApplication::initSingleGame(const std::string name, int playFieldWidth, int playFieldHeight, int numberOfAi)
{
//  cerr << "SingleGame" << endl;
  playerSelf = new Player(name, Player::HUMAN);

  if(playFieldWidth == -1)
  {
    // we will play a scenario
  }
  else
  {
    worldMap = MapGenerator().generateWorld(playFieldWidth, playFieldHeight);
  }
}

void MyGameApplication::initServerGame() {
//  cerr << "ServerGame" << endl;
  playerSelf=new Player(std::string("me"),Player::HUMAN);
  worldMap = MapGenerator().generateWorld(30,30);
}

void MyGameApplication::initClientGame() {
  std::cerr << "ClientGame" << std::endl;
  playerSelf=new Player(std::string("me"),Player::HUMAN);
}
