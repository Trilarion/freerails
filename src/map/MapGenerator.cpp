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
  
  generateWood(map);
  generateDessert(map);
  generateJungle(map);
  generateBog(map);

  // some code for setting:
  // desert, jungle, ice, river, ocean, hills, mountains, ...
  // trees, citys/villages/slums, coal-mine, harbour, farm, ...
  
  return map;
}

void MapGenerator::generateWood(WorldMap* worldMap)
{
  int x, y;
  int howmuch=20;
  for (int i=0; i<howmuch; i++)
  {
    x=(int) (((double)worldMap->getWidth())*rand()/(RAND_MAX+1.0));
    y=(int) (((double)worldMap->getHeight())*rand()/(RAND_MAX+1.0));
    
    if (worldMap->getMapField(x,y)->getType()==MapField::grass)
    {
       worldMap->getMapField(x,y)->setType(MapField::wood);
    }
  }
}

void MapGenerator::generateDessert(WorldMap* worldMap)
{
  int x, y;
  int howmuch=20;
  for (int i=0; i<howmuch; i++)
  {
    x=(int) (((double)worldMap->getWidth())*rand()/(RAND_MAX+1.0));
    y=(int) (((double)worldMap->getHeight())*rand()/(RAND_MAX+1.0));
    
    if (worldMap->getMapField(x,y)->getType()==MapField::grass)
    {
      worldMap->getMapField(x,y)->setType(MapField::dessert);
    }
  }
}

void MapGenerator::generateJungle(WorldMap* worldMap)
{
  int x, y;
  int howmuch=20;
  for (int i=0; i<howmuch; i++)
  {
    x=(int) (((double)worldMap->getWidth())*rand()/(RAND_MAX+1.0));
    y=(int) (((double)worldMap->getHeight())*rand()/(RAND_MAX+1.0));
    
    if (worldMap->getMapField(x,y)->getType()==MapField::grass)
    {
      worldMap->getMapField(x,y)->setType(MapField::jungle);
    }
  }
}

void MapGenerator::generateBog(WorldMap* worldMap)
{
  int x, y;
  int howmuch=50;
  for (int i=0; i<howmuch; i++)
  {
    x=(int) (((double)worldMap->getWidth())*rand()/(RAND_MAX+1.0));
    y=(int) (((double)worldMap->getHeight())*rand()/(RAND_MAX+1.0));
    
    if (worldMap->getMapField(x,y)->getType()==MapField::grass)
    {
      worldMap->getMapField(x,y)->setType(MapField::bog);
    }
  }
}