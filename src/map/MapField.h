/** $Id$
  * Base class for ALL the elements in the game
  */
 
#ifndef __MAPFIELD_H__
#define __MAPFIELD_H__

#include "../elements/GameElement.h"

class MapField {
public:
  /** Constructor */
  MapField();
  /** Destructor */
  virtual ~MapField();

private:
  unsigned short type;
  unsigned short cost;
  unsigned short height;
  GameElement* element;
};

#endif // __MAPFIELD_H__