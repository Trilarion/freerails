/***************************************************************************
                          DoubleTrack.cpp  -  description
                             -------------------
    begin                : Mit Okt 2 2002
    copyright            : (C) 2002 by frank
    email                : frank@laptop
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

#include "DoubleTrack.h"

DoubleTrack::DoubleTrack(unsigned int _connect, Player* _player) :
  Track(0,0, _player, _connect)
{
}

DoubleTrack::~DoubleTrack()
{
}
