/*
 * $Id$
 */


#include "GameController.h"
#include "Player.h"
#include "Track.h"

Track::Track(unsigned int _posX, unsigned int _posY, Player* _player, unsigned int _connect)
     : GamePosElement(_posX, _posY, _player, idTrack)
{
  connect = _connect;
}

Track::~Track()
{
}

void Track::serialize(Serializer* _serializer)
{
  GamePosElement::serialize(_serializer);
  *_serializer << connect;
}

void Track::deserialize(Serializer* _serializer)
{
  GamePosElement::serialize(_serializer);
  *_serializer >> (unsigned int &) connect;
}
