/***************************************************************************
                          StationAddon.h  -  description
                             -------------------
    begin                : Mon Sep 3 2001
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

#ifndef __STATIONADDON_H__
#define __STATIONADDON_H__


/** @short StationAddon class, a singleton shared between \b all the
  * @short Station objects
  * StationAddon does \b not inherit from GameElement because it isn't
  * displayed as a tile, nor does a Player own it.  How it is displayed
  * is up to the GUI clients.
  * @author Bart Vandereycken <bart.vandereycken@student.kuleuven.ac.be>
  * $Id$
  */

#include <string>
#include <vector>
#include <map>

class StationAddon {
public:
  /** Constructor
    * @param file The configuration file
    * @todo Implement the configuration */
  StationAddon(std::string file);
  /** Destructor */
  virtual ~StationAddon();

  /** Set the current year */
  // OBSOLETE
  //void setYear(unsigned int y) { year = y; };

  /** Get all the available addons
    * @param year Year in which to check the addons
    * @return The available addons */
  std::vector<std::string> getAvailable(unsigned int year);

private:
  /** The current year */
  unsigned int year;
  /** All the addons
    * Name of the addon as key, year when available as value */
  std::map<std::string, unsigned int> addons;

};

#endif // __STATIONADDON_H__
