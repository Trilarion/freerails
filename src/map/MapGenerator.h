/** $Id$
  * Base class for ALL the elements in the game
  */
 
#ifndef __MAPGENERATOR_H__
#define __MAPGENERATOR_H__

#include "WorldMap.h"
#include "ControllerDispatcher.h"
#include "City.h"

class MapGenerator {
public:
  /** Constructor */
  MapGenerator();
  /** Destructor */
  virtual ~MapGenerator();
  
  void generateWorld(WorldMap* map, ControllerDispatcher* disp);

private:

  void generateRiver(WorldMap* worldMap);
  void generateOcean(WorldMap* worldMap);
  void generateWood(WorldMap* worldMap);
  void generateDesert(WorldMap* worldMap);
  void generateJungle(WorldMap* worldMap);
  void generateBog(WorldMap* worldMap);
  void generateMountain(WorldMap* worldMap);
  void generateHeight(WorldMap* worldMap);
  void generateCities(WorldMap* worldMap, ControllerDispatcher* disp);
  void generateFarm(WorldMap* worldMap);
  void generateResources(WorldMap* worldMap);
  void generateIndustrie(WorldMap* worldMap);
  bool generateStartPoint(WorldMap* worldMap, int* x, int* y);
  void generateFieldOfType(WorldMap* worldMap, int x, int y, MapField::FieldType type);
};

#endif // __MAPGENERATOR_H__
