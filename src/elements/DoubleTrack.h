/***************************************************************************
                          DoubleTrack.h  -  description
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

#ifndef DOUBLETRACK_H
#define DOUBLETRACK_H

#include "Track.h"

/**
  *@author frank
  */

class DoubleTrack : public Track
{
  public: 
    DoubleTrack(unsigned int connect, Player* _player);
    ~DoubleTrack();
};

#endif