/** $Id$
  * Base class for the players (Human, AI, Nature)
  */

#include "Player.h"

Player::Player(std::string _name, PlayerType _playerType) 
      : GameElement(this, idPlayer)
{
  name = _name;
  playerType = _playerType;
}

Player::~Player()
{
}

void Player::serialize(Serializer* _serializer)
{

  GameElement::serialize(_serializer);
  *_serializer << (const std::string)name;
  *_serializer << playerType;

}

void Player::deserialize(Serializer* _serializer)
{
  GameElement::deserialize(_serializer);
  *_serializer >> name;
  *_serializer >> (int &)playerType;
}

double Player::getMoney()
{
  return money;
}

double Player::incMoney(double _money)
{
  money += money;
  return money;
}
