/***************************************************************************
                          Station.cpp  -  description
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

#include "Station.h"

#include "GameController.h"

Station::Station(GameController* c, Player* p, char* n, Size s, char* t, StationAddon* sA)
       : GameElement(p, idStation)
{
  size = s;
  name = n;
  stationAddon = sA;

  year = 1900;  // Clean up
}

Station::~Station()
{
}

void Station::addAddon(std::string addon)
{
  // Not testing if addon is legal, should be OK ??
  addons[addon] = true;

}

void Station::removeAddon(std::string addon)
{
  // Not testing if addon is legal, should be OK ??
  addons[addon] = false;
}

bool Station::hasAddon(std::string addon)
{
  if(addons.count(addon) == 0)
    return false;
  else
    return addons[addon];
}

std::map<std::string, bool> Station::giveAddons()
{
  std::vector<std::string> availAddons = stationAddon->getAvailable(year);
  for(unsigned int i = 0; i != availAddons.size(); i++)
    if(addons.count(availAddons[i]) == 0)
      addons[availAddons[i]] == false;
  return addons;
}
