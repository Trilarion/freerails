/***************************************************************************
                          StationAddon.cpp  -  description
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

#include "StationAddon.h"

StationAddon::StationAddon(string file) {
  // TODO configuration file...
  // Just some data for now
  addons["Restaurant"]=1900;
  addons["Customs House"]=1900;
  addons["Small Hotel"]=1900;
  addons["Large Hotel"]=1910;
}

StationAddon::~StationAddon(){
}

vector<string> StationAddon::getAvailable(unsigned int year) {
  vector<string> availAddons;
  for(map<string, unsigned int>::iterator It = addons.begin(); It != addons.end(); It++)
    if(year >= (*It).second) availAddons.push_back((*It).first);
  return availAddons;
}
