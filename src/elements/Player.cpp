/** $Id$
  * Base class for the players (Human, AI, Nature)
  */

#include "Player.h"

Player::Player() : GameElement(this, 1)
{
  name = "";
  type = HUMAN;
}

Player::Player(std::string _name, Type _type) : GameElement(this, 1)
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

void Player::deserialize(Serializer* _serializer) {

  GameElement::deserialize(_serializer);
  *_serializer >> name;
  *_serializer >> (int &)type;

}
