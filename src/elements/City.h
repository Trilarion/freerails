/***************************************************************************
                          City.h  -  description
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

#ifndef __CITY_H__
#define __CITY_H__

#include "GameElement.h"
#include "GamePosElement.h"

class City : public GamePosElement  {
public:
  /** Size */

  /** Constructor
    * @param _posX, _posY Position of City
    * @param _name Name
    */
  City(unsigned int _posX, unsigned int _posY, std::string _name);
  /** Destructor */
  ~City();
  
  /** Serialization */
  void serialize(Serializer* _serializer);
  void deserialize(Serializer* _serializer);
  
  // Name
  std::string getName() {return name;};
  void setName(std::string _name) {name = _name;};


private:
  std::string name;
};

#endif
