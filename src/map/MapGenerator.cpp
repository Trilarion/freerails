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
  generateRiver(map);
  generateOcean(map);
  generateWood(map);
  generateDessert(map);
  generateJungle(map);
  generateBog(map);
  generateMountain(map);

  // some code for setting:
  // desert, jungle, ice, river, ocean, hills, mountains, ...
  // trees, citys/villages/slums, coal-mine, harbour, farm, ...
  
  return map;
}

void MapGenerator::generateHeight(WorldMap* worldMap)
{
}

bool MapGenerator::generateStartPoint(WorldMap* worldMap, int* x, int* y)
{
  for (int i; i < 10; i++)
  { *x=(int) (((double)worldMap->getWidth())*rand()/(RAND_MAX+1.0));
    *y=(int) (((double)worldMap->getHeight())*rand()/(RAND_MAX+1.0));
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
  int howmuch=20;
  for (int i=0; i<howmuch; i++)
  {
    if (generateStartPoint(worldMap, &x, &y))
    {
       worldMap->getMapField(x,y)->setType(MapField::river);
       int dir=(int) (4.0*rand()/(RAND_MAX+1.0));
       do {
       x--;
       if (x>=0 || y >=0 || x<worldMap->getWidth() || y<worldMap->getHeight()) break;
       worldMap->getMapField(x,y)->setType(MapField::river);
       } while (x>=0 && y >=0 && x<worldMap->getWidth() && y<worldMap->getHeight());
    }
  }
}

void MapGenerator::generateOcean(WorldMap* worldMap)
{
  int x, y;
  int howmuch=20;
  for (int i=0; i<howmuch; i++)
  {
    x=(int) (((double)worldMap->getWidth())*rand()/(RAND_MAX+1.0));
    y=(int) (((double)worldMap->getHeight())*rand()/(RAND_MAX+1.0));
    
    if (worldMap->getMapField(x,y)->getType()==MapField::grass)
    {
       worldMap->getMapField(x,y)->setType(MapField::ocean);
    }
  }
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

void MapGenerator::generateMountain(WorldMap* worldMap)
{
  int x, y, z;
  int howmuch=50;
  for (int i=0; i<howmuch; i++)
  {
    x=(int) (((double)worldMap->getWidth())*rand()/(RAND_MAX+1.0));
    y=(int) (((double)worldMap->getHeight())*rand()/(RAND_MAX+1.0));
    z=(int) (3.0*rand()/(RAND_MAX+1.0));
    
    if (worldMap->getMapField(x,y)->getType()==MapField::grass)
    {
      switch(z)
      {
        case 0:
          worldMap->getMapField(x,y)->setType(MapField::hills);
	  break;
	case 1:
          worldMap->getMapField(x,y)->setType(MapField::foothills);
	  break;
	case 2:
          worldMap->getMapField(x,y)->setType(MapField::mountain);
	  break;
      }
    }
  }
}