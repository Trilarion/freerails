/** $Id$
  * Base class for ALL the elements in the game
  */
 
#ifndef __MORLDMAP_H__
#define __MORLDMAP_H__

#include "MapField.h"

class WorldMap {
public:
  /** Constructor */
  WorldMap();
  /** Destructor */
  virtual ~WorldMap();
  
private:
  MapField* mapFields;
//  MapInfo* mapInfo;
};

#endif // __WORLDMAP_H__