/** $Id$
  * Class which describe one field on the map
  */

#include "MapField.h"

MapField::MapField(FieldType _type, unsigned short _cost, unsigned short _height) {

  type = _type;
  cost = _cost;
  height = _height;
  element = NULL;
  track = NULL;
}

MapField::~MapField() {
}

