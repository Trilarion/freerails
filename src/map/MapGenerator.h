/** $Id$
  * Base class for ALL the elements in the game
  */
 
#ifndef __MAPGENERATOR_H__
#define __MAPGENERATOR_H__

#include "WorldMap.h"

class MapGenerator {
public:
  /** Constructor */
  MapGenerator();
  /** Destructor */
  virtual ~MapGenerator();
  
  WorldMap* generateWorld(int width, int height);

private:

  void generateWood(WorldMap* worldMap);
  void generateDessert(WorldMap* worldMap);
  void generateJungle(WorldMap* worldMap);
  void generateBog(WorldMap* worldMap);

};

#endif // __MAPGENERATOR_H__