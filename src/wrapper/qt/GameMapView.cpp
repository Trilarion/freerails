/** $Id$
  * View a QCanvas class
  */

#include <stdio.h>

#include "GameMapView.h"

GameMapView::GameMapView(GameMap* map, GameMainWindow* parent, const char* name)
           : QCanvasView(map, parent->getWidget(), name) {


}

GameMapView::GameMapView(GameMap* map, QWidget* parent, const char* name)
           : QCanvasView(map, parent, name) {

}

GameMapView::~GameMapView() {
}

void GameMapView::contentsMousePressEvent(QMouseEvent* e) {
  printf("X:%i\n",e->x());
}