/** $Id$
  * Base class for ALL the elements in the game
  */
 
#ifndef __MORLDMAP_H__
#define __MORLDMAP_H__

#include "MapField.h"

class WorldMap {
public:
  /** Constructor */
  WorldMap(int width, int height);
  /** Destructor */
  virtual ~WorldMap();
  
  MapField* getMapField(int x, int y) {
    if (x < 0 || x > width-1) return NULL;
    if (y < 0 || y > height-1) return NULL;
    return mapFields[y*width+x];
  }
  
  void setMapField(int x, int y, MapField* _mapField) {
    if (x < 0 || x > width-1) return;
    if (y < 0 || y > height-1) return;
    mapFields[y*width+x]=_mapField;
  }
  
  int getWidth() { return width;}

  int getHeight() { return height;}
  
  bool isMapFieldOcean(int x, int y);

private:
  std::vector<MapField *> mapFields;
  int height;
  int width;
//  MapInfo* mapInfo;
};

#endif // __WORLDMAP_H__
