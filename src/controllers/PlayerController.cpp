/** $Id$
  */

#include "PlayerController.h"

PlayerController::PlayerController (): Controller(1) {

}

PlayerController::~PlayerController () {

}

GameElement* PlayerController::CreateElement(Serializer* _serializer) {

  Player* player = new Player();
  player->serialize(_serializer);
  return player;
}
