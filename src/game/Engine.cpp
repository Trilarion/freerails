/** $Id$
  */

#include "Engine.h"

Engine::Engine(WorldMap* _worldMap, Player* _player) {

  worldMap = _worldMap;
  isSingle = true;
  isClient = false;
  isServer = false;
  lastmsec = 0;
  frame = 0;
  
  gameCon=new GameController("default",1900,1,1);
  cerr << "engine inited" << lastmsec << endl;
//gameCon.addPlayer(_player);

}

Engine::~Engine() {

}

void Engine::startGame() {

  if (gameCon->startGame()) {
    if (isServer) {
      // Server.sendAll("Game started");
    }
  }

}

void Engine::checkNext(int msec) {

  if (gameCon->getState()==GameController::Running)
  {
    if ((msec-lastmsec)>10)
    { lastmsec=msec;
      frame++;
      process();
      cerr << frame << endl;
    }
  }

}

void Engine::process() {

  cerr << ".";
  if(isServer) { }

}