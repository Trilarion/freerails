/** $Id$
  */
#include <iostream>

#include "Engine.h"
#include "MapGenerator.h"
#include "FreeRailsLog.h"

/* single player */
Engine::Engine(Player* _player, int w, int h)
{
  worldMap = MapGenerator().generateWorld(w, h);
  isSingle = true;
  isClient = false;
  isServer = false;

  /* Serializer Was Created...right .. Static member of Connection.
     so we just have to grab it... */
  Connection con;
  serializer = con.getSerializer();
  
  Init(_player, worldMap);

  std::cerr << "engine(alone) inited" << std::endl;
}


/* server game */
Engine::Engine(Player* _player, int w, int h, int port)
{
  gameState = Initializing;

  worldMap = MapGenerator().generateWorld(w, h);


  
  server=new Server(port);

  /* OH WELL ... HARD DECISION */
  serializer = server->getSerializer();
  /* server=_server; */
  isSingle = false;
  isClient = false;
  isServer = true;
  
  Init(_player, worldMap);
  
   std::cerr << "engine(Server) inited" << std::endl;
}


/* client game */
Engine::Engine(Player* _player, int w, int h, char *server, int port)
{
  gameState = Initializing;
  
  worldMap = NULL;

  client=new Client();

  /* OH WELL ... HARD DECISION */
  serializer = client->getSerializer();

  client->open(server,port);
  FreeRailsLog("CLIENT GAME INIT ENGINE: %i",client->getState());
  client->joinGame(_player, serializer);
  FreeRailsLog("CLIENT GAME INIT ENGINE");


  /* client=_client; */
  
  isSingle = false;
  isClient = true;
  isServer = false;
  
  //  Init(_player, _worldMap); // Must be done Later!
  
  std::cerr << "engine(Client) inited" << std::endl;
}

void Engine::Init(Player* _player, WorldMap* _worldMap)
{
  lastmsec = 0;
  frame = 0;
  gui2engine = new MessageQueue();
  engine2gui = new MessageQueue();

  controllerDispatcher = new ControllerDispatcher();
  
  controllerDispatcher->addController(new PlayerController());

  controllerDispatcher->addController(new TrackController(_worldMap));
  controllerDispatcher->addController(new StationController(_worldMap));
  
  controllerDispatcher->getController(GameElement::idPlayer)->addGameElement(_player);

  gameState = Waiting;

  gameCon = new GameController("default", 1900, 1, 1);
}

Engine::~Engine()
{

}

void Engine::sendMsg(Message* msg)
{
  gui2engine->addMsg(msg);
}

bool Engine::haveMsg()
{
  return engine2gui->hasMoreElements();
}

Message* Engine::getMsg() {

  return engine2gui->getMsg();
}

void Engine::checkNet() {
  if (isServer) {
    server->check();
  } else
  if (isClient) {
    //Client.check();
  }
}

void Engine::checkNext(int msec)
{
  if (gameState == Running)
  {
    if ((msec - lastmsec) > 10)
    {
      lastmsec = msec;
      frame++;
      process();
//      std::cerr << frame << std::endl;
    }
  }
  while (gui2engine->hasMoreElements())
  {
    Message* msg = gui2engine->getMsg();
    processMsg(msg);
    delete msg;
  }

}

void Engine::process()
{
  /*  std::cerr << "."; */
  if(isServer)
  {
  }
}

void Engine::processMsg(Message* msg)
{
  std::cerr << "\nin processMsg(Message*)\n";
  switch (msg->getMsgType())
  {
    case Message::addElement:
      std::cerr << "\nadd element\n";
      addElementToGame(msg);
      if (msg->getData() != NULL)
        delete (GameElement*)msg->getData();
      break;
    case Message::stateOfGame:
      std::cerr << "change state of the game";
      changeStateOfGame(msg);
      break;
    default:
      break;
  }
}

void Engine::addElementToGame(Message* msg)
{
  GameElement* element = (GameElement *)msg->getData();
  Controller* elementController = controllerDispatcher->getController(element->getTypeID());
  if (elementController==NULL)
  {
    std::cerr << "\nNo Controller found for Type: "<< element->getTypeID() <<"\n";
  } else
  {
    if (elementController->canBuildElement(element))
    {
      elementController->addGameElement(element);
      Message* msgBack = new Message(Message::addElement, 0, element);
      SendAll(msgBack);
    }
  }
}

void Engine::changeStateOfGame(Message* msg)
{
  GameState state = *(GameState *)msg->getData();
  Message* Msg = new Message(Message::stateOfGame, GameElement::idNone, &state);
  if ((gameState == Waiting) && (state == Running))
  {
    gameState = state;
    SendAll(Msg);
  }
  if ((gameState == Running) && (state == Pausing))
  {
    gameState = state;
    SendAll(Msg);
  }
  if ((gameState == Pausing) && (state == Running))
  {
    gameState = state;
    SendAll(Msg);
  }
  if (((gameState == Pausing) || (gameState == Running)) && (state == Stopping))
  {
    gameState = state;
    SendAll(Msg);
  }
}

void Engine::SendAll(Message* msg)
{
   std::cerr << "in SendAll(Message*)\n Server is " << isServer << std::endl;
  if (isServer)
  {
    // Server
  }
  else
  {
    engine2gui->addMsg(msg);
  }
}
