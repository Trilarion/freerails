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
  
  generateHeight(map);
  generateOcean(map);
  generateRiver(map);
  generateOcean(map);
  generateWood(map);
  generateDessert(map);
  generateJungle(map);
  generateBog(map);
  generateMountain(map);
  generateCities(map);
  generateResources(map);
  generateIndustrie(map);

  // some code for setting:
  // desert, jungle, ice, river, ocean, hills, mountains, ...
  // trees, citys/villages/slums, coal-mine, harbour, farm, ...
  
  return map;
}

void MapGenerator::generateHeight(WorldMap* /* worldMap */)
{
}

bool MapGenerator::generateStartPoint(WorldMap* worldMap, int* x, int* y)
{
  for (int i=0; i < 10; i++)
  {
    *x = int (((double)worldMap->getWidth())*rand()/(RAND_MAX+1.0));
    *y = int (((double)worldMap->getHeight())*rand()/(RAND_MAX+1.0));
    if (worldMap->getMapField(*x,*y)->getType()==MapField::grass)
    {
      return true;
    }
  }
  return false;
}

void MapGenerator::generateRiver(WorldMap* worldMap)
{
  int x, y;
  int howmuch=worldMap->getWidth()*worldMap->getHeight()/100;
  for (int i=0; i<howmuch; i++)
  {
    if (generateStartPoint(worldMap, &x, &y))
    {
       worldMap->getMapField(x,y)->setType(MapField::river);
       int dir=(int) (4.0*rand()/(RAND_MAX+1.0));
       do {
         switch (dir)
	 {
	   case 0: x--; break;
	   case 1: y--; break;
	   case 2: x++; break;
	   case 3: y++; break;
	 }
         if (x<0 || y <0 || x>=worldMap->getWidth() || y>=worldMap->getHeight()) break; // go out of the map
	 if (worldMap->getMapField(x,y)->getType()==MapField::river) break; // go in another river
	 if (worldMap->getMapField(x,y)->getType()==MapField::ocean) break; // go in an ocean
         worldMap->getMapField(x,y)->setType(MapField::river);
       } while (x>=0 && y >=0 && x<worldMap->getWidth() && y<worldMap->getHeight());
    }
  }
}

void MapGenerator::generateOcean(WorldMap* worldMap)
{
  int x, y;
  int howmuch=worldMap->getWidth()*worldMap->getHeight()/200;
  for (int i=0; i<howmuch; i++)
  {
    if (generateStartPoint(worldMap, &x, &y))
    {
       worldMap->getMapField(x,y)->setType(MapField::ocean);
       for (int ii=0; ii<3; ii++)
       {
         generateFieldOfType(worldMap,x,y,MapField::ocean);
       }
    }
  }
}

void MapGenerator::generateFieldOfType(WorldMap* worldMap,int x,int y,MapField::FieldType type)
{
  int dir=(int) (5.0*rand()/(RAND_MAX+1.0));
  switch (dir)
  {
    case 0: x--; break;
    case 1: y--; break;
    case 2: x++; break;
    case 3: y++; break;
    case 4: return;
  }
  if (x<0 || y <0 || x>=worldMap->getWidth() || y>=worldMap->getHeight()) return; // go out of the map
  if (worldMap->getMapField(x,y)->getType()==type) return;
  if (worldMap->getMapField(x,y)->getType()==MapField::grass)
  {
    worldMap->getMapField(x,y)->setType(type);
    generateFieldOfType(worldMap,x,y,type);
  }
  generateFieldOfType(worldMap,x,y,type);
}

void MapGenerator::generateWood(WorldMap* worldMap)
{
  int x, y;
  int howmuch=worldMap->getWidth()*worldMap->getHeight()/200;
  for (int i=0; i<howmuch; i++)
  {
    if (generateStartPoint(worldMap, &x, &y))
    {
       worldMap->getMapField(x,y)->setType(MapField::wood);
       for (int ii=0; ii<3; ii++)
       {
         generateFieldOfType(worldMap,x,y,MapField::wood);
       }
    }
  }
}

void MapGenerator::generateDessert(WorldMap* worldMap)
{
  int x, y;
  int howmuch=worldMap->getWidth()*worldMap->getHeight()/200;
  for (int i=0; i<howmuch; i++)
  {
    if (generateStartPoint(worldMap, &x, &y))
    {
       worldMap->getMapField(x,y)->setType(MapField::dessert);
       for (int ii=0; ii<3; ii++)
       {
         generateFieldOfType(worldMap,x,y,MapField::dessert);
       }
    }
  }
}

void MapGenerator::generateJungle(WorldMap* worldMap)
{
  int x, y;
  int howmuch=worldMap->getWidth()*worldMap->getHeight()/200;
  for (int i=0; i<howmuch; i++)
  {
    if (generateStartPoint(worldMap, &x, &y))
    {
       worldMap->getMapField(x,y)->setType(MapField::jungle);
       for (int ii=0; ii<3; ii++)
       {
         generateFieldOfType(worldMap,x,y,MapField::jungle);
       }
    }
  }
}

void MapGenerator::generateBog(WorldMap* worldMap)
{
  int x, y;
  int howmuch=worldMap->getWidth()*worldMap->getHeight()/200;
  for (int i=0; i<howmuch; i++)
  {
    if (generateStartPoint(worldMap, &x, &y))
    {
       worldMap->getMapField(x,y)->setType(MapField::bog);
       for (int ii=0; ii<2; ii++)
       {
         generateFieldOfType(worldMap,x,y,MapField::bog);
       }
    }
  }
}

void MapGenerator::generateMountain(WorldMap* worldMap)
{
  int x, y, z;
  int howmuch=worldMap->getWidth()*worldMap->getHeight()/200;
  for (int i=0; i<howmuch; i++)
  {
    z=(int) (3.0*rand()/(RAND_MAX+1.0));
    if (generateStartPoint(worldMap, &x, &y))
    {
      MapField::FieldType type;
      switch(z)
      {
        case 0:
          type=MapField::hills;
	  break;
	case 1:
          type=MapField::foothills;
	  break;
	case 2:
          type=MapField::mountain;
	  break;
      }
       worldMap->getMapField(x,y)->setType(type);
       for (int ii=0; ii<2; ii++)
       {
         generateFieldOfType(worldMap,x,y,type);
       }
    }
  }
}

void MapGenerator::generateCities(WorldMap* worldMap)
{
  int x, y;
  int i, ii;
  int howmuch = worldMap->getWidth() * worldMap->getHeight() / 500;
  for (i=0; i<howmuch; i++)
  {
    if (generateStartPoint(worldMap, &x, &y))
    {
       worldMap->getMapField(x, y)->setType(MapField::village);
       for (ii=0; ii<3; ii++)
       {
         generateFieldOfType(worldMap, x, y, MapField::village);
       }
    }
  }
}

void MapGenerator::generateFarm(WorldMap* worldMap)
{
  int x, y;
  int howmuch=worldMap->getWidth()*worldMap->getHeight()/200;
  for (int i=0; i<howmuch; i++)
  {
    if (generateStartPoint(worldMap, &x, &y))
    {
       worldMap->getMapField(x,y)->setType(MapField::farm);
    }
  }
}

void MapGenerator::generateResources(WorldMap* worldMap)
{
  int x, y;
  int howmuch=worldMap->getWidth()*worldMap->getHeight()/200;
  for (int i=0; i<howmuch; i++)
  {
    if (generateStartPoint(worldMap, &x, &y))
    {
       worldMap->getMapField(x,y)->setType(MapField::resource);
    }
  }
}

void MapGenerator::generateIndustrie(WorldMap* worldMap)
{
  int x, y;
  int howmuch=worldMap->getWidth()*worldMap->getHeight()/200;
  for (int i=0; i<howmuch; i++)
  {
    if (generateStartPoint(worldMap, &x, &y))
    {
       worldMap->getMapField(x,y)->setType(MapField::industrie);
    }
  }
}
