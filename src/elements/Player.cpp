/** $Id
  * Base class for the players (Human, AI, Nature)
  */

#include "Player.h"

Player::Player(char* n) {
  name = n;
}

Player::~Player() {
}

inline char* Player::getName() {
  return name;
}

void Player::addGameElement(GameElement* element) {
  elements.push_back(element);
}