/** $Id$
  * Map class (handling and viewing)
  */

#include "GameMap.h"

GameMap::GameMap(GameMainWindow* parent, const char* name)
       : QCanvas(parent->getWidget(), name) {

}

GameMap::GameMap(QWidget* parent, const char* name)
       : QCanvas(parent, name) {

}

GameMap::~GameMap() {
}
