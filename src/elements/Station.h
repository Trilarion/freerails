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
#include "Player.h"
#include "StationAddon.h"
#include <map>
#include <vector>
#include <string>


/** @short Station class
  *
  * @author Bart Vandereycken <bart.vandereycken@student.kuleuven.ac.be>
  * $Id$
  */
class Station : public GameElement  {
public:
  /** Size */
  enum Size {Small = 0, Medium, Big};


  /** Constructor
    * @param p Owner (Human or AI \b not Nature!)
    * @param n Name
    * @param t Tileset location
    * @param a All StationAddons in the game (available or not)
    * @param sA The common StationAddon
    */
  Station(Player* p, char* n, Size s, char* t, StationAddon* sA);
  /** Destructor */
  ~Station();
  /** Adds a station addon
    * @warning There can only be one type of each addon
    * @param addon Addon to the station */
  void addAddon(string addon);
  /** Removes a station addon
    * @param addon Addon of the station */
  void removeAddon(string addon);
  /** Check whether a station addon is built
    * @param addon Addon of the station */
  bool hasAddon(string addon);
  /** Gives all the possible addons.
    * This is only usefull for the UI when building/removing addons
    * @return Map with addon as key, true if build as value */
  map<string, bool> giveAddons();

  /** The current year
    * @warning Just temporary, must be done with the GameController??
    * @todo Clean up this interface */
  unsigned int year;


private:
  char* name;
  /** The singleton with addon info */
  StationAddon* stationAddon;
  Size size;
  /** Vector containing all the possible station addons
    * @warning They don't have to be available right now, but may be eventually */
  vector<string> allAddons;
  /** Map containing all station addons
    * Value is true if built.
    * @warning Value is not really used except for giveAddons() */
  map<string, bool> addons;
};

#endif
