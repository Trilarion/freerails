/** $Id$
  * Base class for the players (Human, AI, Nature)
  */

#include "Player.h"

Player::Player(std::string _name, Type _type) : GameElement(this, idPlayer)
{
  name = _name;
  type = _type;
}

Player::~Player()
{
}

void Player::serialize(Serializer* _serializer)
{

  GameElement::serialize(_serializer);
  *_serializer << (const std::string)name;
  *_serializer << type;

}

void Player::deserialize(Serializer* _serializer)
{
  GameElement::deserialize(_serializer);
  *_serializer >> name;
  *_serializer >> (int &)type;
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
