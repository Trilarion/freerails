/*
 * $Id$
 * GameController class
 */

#include "GameController.h"

#include "GameElement.h"
#include "Player.h"
#include "Station.h"
#include "GameInfo.h"

GameController::GameController()
{
  inited = false;
  state = NotStarted;
  // Should we do anything else???
}

GameController::GameController(char* gamename, short int year, short int month,
    short int day)
{
  inited = false;
  state = NotStarted;
  init(gamename, year, month, day);
}

GameController::~GameController()
{
  if(state == Running || state == Paused)
    // TODO: send warning to clients
    stopGame();
  if(inited)
  {
    delete info;
    delete startinfo;
  }
}

bool GameController::init(char* name, short int year, short int month,
    short int day)
{
  if(inited)
    return false;
  lastid = 0;
  // TODO: Check if date is OK
  info = new GameInfo(this, name, year, month, day);
  // GameInfo::operator= ???
  startinfo = new GameInfo(this, name, year, month, day);
  inited = true;
  return true;
}

idtype GameController::addElement(GameElement* e)
{
  // TODO: Check if action is valid
  // Maybe bad id generation possibility, but it's faster than to search for
  //  first unused id
  idtype newid = ++lastid;
  idmap[newid] = e;
  if(e->rtti() == RTTI_STATION)
    stations.push_back((Station*)e);
  else
    elements.push_back(e);
  return newid;
}

void GameController::removeElement(idtype id)
{
  // TODO: Check if action is valid
  GameElement* e = idmap[id];
  idmap.erase(id);
  if(e->rtti() == RTTI_STATION)
    stations.erase((Station**)e);
  else
    elements.erase((GameElement**)e);
}

bool GameController::addPlayer(Player* p)
{
  // TODO: Check if action is valid ??
  players.push_back(p);
}

bool GameController::startGame()
{
  // Return some enum { GameStarted, NotInited, CannotInitNet ... } ???
  if(!inited)
    return false;
  // Init game here
  state = Running;
  return true;
}

void GameController::pauseGame()
{
  // Return bool or smth?
  if(state == Running)
    state = Paused;
  else if(state == Paused)
    state = Running;
  else
    return;
}

void GameController::stopGame()
{
  // Return bool or smth?
  if(state != Running && state != Paused)
    return;
  // TODO: send warning to clients
  // Delete game's data
  delete info;
  delete startinfo;
  //delete worldmap;
  // Empty vectors
  players.clear();
  elements.clear();
  stations.clear();
  idmap.clear();
  // Reinit variables
  inited = false;
  state = Stopped;
}
