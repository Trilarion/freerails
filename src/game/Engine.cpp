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
  
  gui2engine=new MessageQueue();
  engine2gui=new MessageQueue();

  gameCon=NULL;  
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

void Engine::sendMsg(Message* msg) {

  gui2engine->addMsg(msg);
}

bool Engine::haveMsg() {

  return engine2gui->hasMoreElements();
}

Message* Engine::getMsg() {

  return engine2gui->getMsg();
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