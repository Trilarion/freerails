/** $Id$
  * Class which hold the full world
  */

#include "WorldMap.h"

WorldMap::WorldMap(int _width, int _height) {

  width=_width;
  height=_height;
  
  mapFields.resize(width*height);
  
  for (int y=0; y < height; y++) {
    for (int x=0; x < width; x++) {
      mapFields[y*width+x]=new MapField(0,0,0);
    }
  }  

}

WorldMap::~WorldMap() {
  for (int y=0; y < height; y++) {
    for (int x=0; x < width; x++) {
      delete mapFields[y*width+x];
    }
  }  
}

