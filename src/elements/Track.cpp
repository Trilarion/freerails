/*
 * $Id$
 */


#include "GameController.h"
#include "Player.h"
#include "Track.h"

Track::Track(unsigned int _connect, Player* _player) : GameElement(_player, idTrack)
{
  connect = _connect;
}

Track::~Track()
{
}

void Track::serialize(Serializer* _serializer)
{
  GameElement::serialize(_serializer);
  *_serializer << connect;
}

void Track::deserialize(Serializer* _serializer)
{
  GameElement::serialize(_serializer);
  *_serializer >> (unsigned int &) connect;
}
