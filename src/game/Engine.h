/** $Id$
  */
 
#ifndef __ENGINE_H__
#define __ENGINE_H__

#include "WorldMap.h"
#include "Player.h"
#include "GameController.h"
#include "MessageQueue.h"

class Engine {
public:

  /** Constructor */
  Engine(WorldMap* _worldMap, Player* _player);  // For a single game
  /** Destructor */
  virtual ~Engine();
  
  void checkNext(int msec); // This is the function wich will called from ParaGUI or Qt by every idle Message 
                            // The function will check the time and connections and let then play all :-)
			    
  void startGame();  // starts the Game and sends out to all clients (if network/server mode)
  
  void sendMsg(Message* msg);
  bool haveMsg();
  Message* getMsg();

private:

  void process();
  
  MessageQueue* gui2engine;
  MessageQueue* engine2gui;

  WorldMap* worldMap;
  bool isClient;
  bool isServer;
  bool isSingle;
  GameController* gameCon;
  int lastmsec;
  int frame;

};

#endif // __ENGINE_H__