/***************************************************************************
                          Station.h  -  description
                             -------------------
    begin                : Sat Sep 1 2001
    copyright            : (C) 2001 by Freerails developers
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

#ifndef __STATION_H__
#define __STATION_H__

#include "GameElement.h"
#include "GamePosElement.h"
#include "StationAddon.h"
#include <map>
#include <vector>
#include <string>

#define RTTI_STATION 1

class GameController;

/** @short Station class
  *
  * @author Bart Vandereycken <bart.vandereycken@student.kuleuven.ac.be>
  * $Id$
  */
class Station : public GamePosElement  {
public:
  /** Size */
  enum Size {Signal = 0, Small, Medium, Big};


  /** Constructor
    * @param _posX, _posY Position of Station
    * @param _player Owner (Human or AI \b not Nature!)
    * @param _name Name
    * @param _size The Size of the Station
    * @param sA The common StationAddon
    */
  Station(unsigned int _posX, unsigned int _posY, Player* _player, std::string _name, Size _size, StationAddon* sA);
  /** Destructor */
  ~Station();
  
  /** Serialization */
  void serialize(Serializer* _serializer);
  void deserialize(Serializer* _serializer);
  
  // Name
  std::string getName() {return name;};
  void setName(std::string _name) {name = _name;};

  // Size
  Size getSize() {return size;};
  void setSize(Size _size) {size = _size;};

  /** Adds a station addon
    * @warning There can only be one type of each addon
    * @param addon Addon to the station */
  void addAddon(std::string addon);
  /** Removes a station addon
    * @param addon Addon of the station */
  void removeAddon(std::string addon);
  /** Check whether a station addon is built
    * @param addon Addon of the station */
  bool hasAddon(std::string addon);
  /** Gives all the possible addons.
    * This is only usefull for the UI when building/removing addons
    * @return Map with addon as key, true if build as value */
  std::map<std::string, bool> giveAddons();


private:
  std::string name;
  Size size;
  /** The singleton with addon info */
  StationAddon* stationAddon;
  /** Vector containing all the possible station addons
    * @warning They don't have to be available right now, but may be eventually */
  std::vector<std::string> allAddons;
  /** Map containing all station addons
    * Value is true if built.
    * @warning Value is not really used except for giveAddons() */
  std::map<std::string, bool> addons;
};

#endif
