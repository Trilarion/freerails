/** $Id$
  */
 
#ifndef __ENGINE_H__
#define __ENGINE_H__

#include "ControllerDispatcher.h"
#include "PlayerController.h"

#include "WorldMap.h"
#include "Player.h"
#include "GameController.h"
#include "MessageQueue.h"
#include "Client.h"
#include "Server.h"

class TrackController;
class StationController;

class Engine
{
  public:

    enum GameState { ask=0, Initializing, Waiting, Starting, Running, Pausing, Stopping, Error };

    /** Constructor */
    Engine(WorldMap* _worldMap, Player* _player);                   // For a single game
    Engine(WorldMap* _worldMap, Player* _player, Server* _server);  // For a Server game
    Engine(Player* _player, Client* _client);                       // For a Client game
    /** Destructor */
    virtual ~Engine();

    void checkNet();
    void checkNext(int msec); // This is the function wich will called from ParaGUI or Qt by there timer
                              // The function will check the time and connections and let then play all :-)


    void sendMsg(Message* msg); // The GUI can send a message to the engines MessageQueue
    bool haveMsg();             // is true if the engine have a message for the GUI
    Message* getMsg();          // get the message for the GUI, is NULL if there is no Message

    ControllerDispatcher* getControllerDispatcher() {return controllerDispatcher;};




    WorldMap* getWorldMap() {return worldMap;};

    GameState getGameState() {return gameState;};

  private:

    void process();    // process one day(?) in game

  // The 5 different MessageTypes
    void addElementToGame(Message* msg);  // add an incomming element to the Game
    void changeStateOfGame(Message* msg);  // change state between Running and Pausing

    void processMsg(Message* msg); // processes one given message

    void Init(Player* _player);  // Initialize the Engine

    void SendAll(Message* msg);  // sends msg to all, if not isServer then all is only the GUI

    MessageQueue* gui2engine;  // The MessageQueue's between GUI and Engine
    MessageQueue* engine2gui;

    MessageQueue* net2engine;  // The MessageQueue's between net and Engine
    MessageQueue* engine2net;


    WorldMap* worldMap;  // Points to the worldmap
    Server* server;      // Points to the Server connection or to NULL
    Client* client;      // Points to the Client connection or to NULL

    bool isClient;
    bool isServer;
    bool isSingle;

    int lastmsec;         // time when checkNext was last called
    int frame;            // the Frame in which the game is at the moment (Not used yet)
    GameState gameState;  // state of the Game

    ControllerDispatcher* controllerDispatcher;  // Holds the controllerDispatcher

    TrackController *trackControl;
    StationController *stationControl;



    GameController* gameCon; // Is it needed any longer?
  
};

#endif // __ENGINE_H__
