/** $Id$
  * Class which generate a full world
  */

#include "MapGenerator.h"

MapGenerator::MapGenerator() {

}

MapGenerator::~MapGenerator() {
}

WorldMap* MapGenerator::generateWorld(int width, int height) {

  WorldMap* map;
  map = new WorldMap(width, height);
  
  // some code for setting:
  // desert, jungle, ice, river, ocean, hills, mountains, ...
  // trees, citys/villages/slums, coal-mine, harbour, farm, ...
  
  return map;
}