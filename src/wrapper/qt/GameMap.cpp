/** $Id$
  * Map class (handling and viewing)
  */

#include "GameMap.h"

#include "GameMap.moc"

GameMap::GameMap(GameMainWindow* parent, const char* name)
       : QCanvas(parent->getWidget(), name) {

	view = new GameMapView(this, parent, "view");
}

GameMap::GameMap(QWidget* parent, const char* name)
       : QCanvas(parent, name) {

	view = new GameMapView(this, parent, "view");
}

GameMap::~GameMap() {
	delete view;
}

void GameMap::Show() {
	view->show();
}
