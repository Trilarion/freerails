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
  gameCon->addPlayer(_player);

  cerr << "engine inited" << lastmsec << endl;
}

Engine::~Engine() {

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

  while (gui2engine->hasMoreElements()) {
    Message* msg = gui2engine->getMsg();
    processMsg(msg);
    delete msg;
  }

}

void Engine::process() {

  cerr << ".";
  if(isServer) { }

}

void Engine::processMsg(Message* msg) {

  switch (msg->getType()) {
    case Message::startGame: startGame();
    case Message::pauseGame: pauseGame();
  }

}

void Engine::startGame() {

  cerr << "Start Game" << endl;
  if (gameCon->startGame()) {
    if (isServer) {
      // Server.sendAll("Game started");
    }
  }

}

void Engine::pauseGame() {

  cerr << "Pause/Unpause Game" << endl;
  gameCon->pauseGame();
  if (isServer) {
      // Server.sendAll("Game paused");
  }
}

int Engine::canBuildTrack(int x, int y, int type, int dir) {

  MapField* field=worldMap->getMapField(x,y);
  
  if (worldMap->isMapFieldOcean(x,y)) return -1;

  return 0;
}