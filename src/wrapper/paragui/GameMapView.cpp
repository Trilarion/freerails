/*
 * $Id$
 */

#include "GameMapView.h"

GameMapView::GameMapView(GameMainWindow* parent, int x, int y, int w, int h, WorldMap* _worldMap):
PG_GradientWidget(parent->getWidget(), PG_Rect(x,y,w,h), "GradientWidget") {
  SetBackgroundBlend(0);
  worldMap=_worldMap;
}

GameMapView::~GameMapView() {
}