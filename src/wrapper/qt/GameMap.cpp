/** $Id$
  * Map class (handling and viewing)
  */

#include "Engine.h"
#include "GameMainWindow.h"
#include "GameMap.h"

GameMap::GameMap(Engine *_engine, GameMainWindow* parent, const char* name)
       : QCanvas(_engine->getWorldMap()->getWidth() * 30, _engine->getWorldMap()->getHeight() * 30)
{
  engine = _engine;
//  int xw = ;
//  int yw = ;
//
//  setTiles(pixmap, 30, 30, 30, 30);
}

GameMap::~GameMap()
{
}
