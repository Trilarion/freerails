/** $Id$
  */

#include "PlayerController.h"

PlayerController::PlayerController (): Controller(NULL, GameElement::idPlayer)
{

}

PlayerController::~PlayerController ()
{

}

GameElement* PlayerController::CreateElement(Serializer* _serializer)
{
  Player* player = new Player("George Stephenson", Player::HUMAN);
  player->serialize(_serializer);
  return player;
}
