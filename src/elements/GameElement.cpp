/** $Id$
  * Base class for ALL the elements in the game
  */

#include "GameElement.h"

#include "Player.h"

GameElement::GameElement(GameController* c, Player* p, char* filename) {
  player = p;
  tileset = filename;
  controller = c;
  registered = false;
  id = 0;
}

GameElement::~GameElement() {
}

void GameElement::setPlayer(Player* p) {
  player = p;
}

Player* GameElement::getPlayer() {
  return player;
}

void GameElement::update() {
}

bool GameElement::registerSelf() {
  // Return immidiately if element is already registered
  if(registered == true)
    return false;
  id = controller->addElement(this);
  if(id == 0)
  {
    // error
    return false;
  }
  else
  {
    registered = true;
    return true;
  }
}
