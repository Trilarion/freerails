/** $Id$
  * Base class for ALL the elements in the game which have positions
  */

#include "GamePosElement.h"

GamePosElement::GamePosElement(unsigned int _posX, unsigned int _posY, Player* _player, TypeID _typeID)
              : GameElement(_player, _typeID)
{
  posX = _posX;
  posY = _posY;
}

GamePosElement::~GamePosElement()
{
}

void GamePosElement::serialize(Serializer* _serializer)
{
  GameElement::serialize(_serializer);
  *_serializer << posX;
  *_serializer << posY;
}

void GamePosElement::deserialize(Serializer* _serializer)
{
  GameElement::deserialize(_serializer);
  *_serializer >> (long int &)posX;
  *_serializer >> (long int &)posY;
}
