/** $Id$
  * Base class for ALL the elements in the game
  */

#include "GameElement.h"

GameElement::GameElement(Player* p, char* filename) {
  player = p;
  tileset = filename;
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

