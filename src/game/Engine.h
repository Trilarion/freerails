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
			    
  void sendMsg(Message* msg); // send a message to the engines MessageQueue
  bool haveMsg();             // is true if the engine have a message for you
  Message* getMsg();          // get the message for you, is NULL if there is no Message
  
  int canBuildTrack(int x, int y, int type, int dir); // return the monay is needed to build
						      // or -1 for not buildable
  WorldMap* getWorldMap() {return worldMap;};

private:

  void process();    // process one day in game
  void startGame();  // starts the Game and sends out to all clients (if network/server mode)
  void pauseGame();  // starts or ends pausing the Game and sends out to all clients (if network/server mode)
  void processMsg(Message* msg); // processes one given message
  
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