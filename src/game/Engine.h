/** $Id$
  */
 
#ifndef __ENGINE_H__
#define __ENGINE_H__

#include "WorldMap.h"
#include "Player.h"
#include "GameController.h"

class Engine {
public:

  /** Constructor */
  Engine(WorldMap* _worldMap, Player* _player);  // For a single game
  /** Destructor */
  virtual ~Engine();
  
  void checkNext(int msec); // This is the function wich will called from ParaGUI or Qt by every idle Message 
                            // The function will check the time and connections and let then play all :-)
			    
  void startGame();  // starts the Game and sends out to all clients (if network/server mode)

private:

  void process();

  WorldMap* worldMap;
  bool isClient;
  bool isServer;
  bool isSingle;
  GameController* gameCon;
  int lastmsec;
  int frame;

};

#endif // __ENGINE_H__