/** $Id$
  * Map class (handling and viewing)
  */

#include "GuiEngine.h"
#include "GameMainWindow.h"
#include "GameMap.h"

GameMap::GameMap(GuiEngine *_guiEngine, GameMainWindow */*parent*/, const char */*name*/)
       : QCanvas(_guiEngine->getWorldMap()->getWidth() * 30, _guiEngine->getWorldMap()->getHeight() * 30)
{
  guiEngine = _guiEngine;
}

GameMap::~GameMap()
{
}
