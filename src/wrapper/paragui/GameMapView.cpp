/*
 * $Id$
 */

#include "GameMapView.h"

GameMapView::GameMapView(GameMainWindow* parent, int x, int y, int w, int h):
PG_GradientWidget(parent->getWidget(), PG_Rect(x,y,w,h), "GradientWidget") {
  SetBackgroundBlend(0);
}

GameMapView::~GameMapView() {
}