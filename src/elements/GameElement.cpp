/** $Id$
  * Base class for ALL the elements in the game
  */

#include "GameElement.h"

GameElement::GameElement(Player* _player, TypeID _typeID)
{
  player = _player;
  typeID = _typeID;
}

GameElement::~GameElement()
{
}

void GameElement::serialize(Serializer* _serializer)
{
  *_serializer << elementID;
}

void GameElement::deserialize(Serializer* _serializer)
{
  *_serializer >> (ElementID &)elementID;
}

void GameElement::setPlayer(Player* p)
{
  player = p;
}

Player* GameElement::getPlayer()
{
  return player;
}

void GameElement::update()
{
}
