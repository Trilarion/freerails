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

  void generateRiver(WorldMap* worldMap);
  void generateOcean(WorldMap* worldMap);
  void generateWood(WorldMap* worldMap);
  void generateDessert(WorldMap* worldMap);
  void generateJungle(WorldMap* worldMap);
  void generateBog(WorldMap* worldMap);
  void generateMountain(WorldMap* worldMap);
  void generateHeight(WorldMap* worldMap);
  bool generateStartPoint(WorldMap* worldMap, int* x, int* y);
  void generateFieldOfType(WorldMap* worldMap, int x, int y, MapField::FieldType type);
};

#endif // __MAPGENERATOR_H__
