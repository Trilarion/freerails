/***************************************************************************
                          City.cpp  -  description
                             -------------------
    begin                : Sat May 10 2003
    copyright            : (C) 2001-2003 by Freerails developers
    email                :
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

#include "City.h"

City::City(unsigned int _posX, unsigned int _posY, std::string _name)
       : GamePosElement(_posX, _posY, NULL, idCity)
{
  name = _name;
}

City::~City()
{
}

void City::serialize(Serializer* _serializer)
{
  GamePosElement::serialize(_serializer);
  *_serializer << (const std::string)name;
}

void City::deserialize(Serializer* _serializer)
{
  GamePosElement::deserialize(_serializer);
  *_serializer >> name;
}
