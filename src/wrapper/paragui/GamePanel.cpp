/*
 * $Id$
 */

#include "GamePanel.h"

GamePanel::GamePanel(GameMainWindow* parent, int x, int y, int w, int h):
PG_GradientWidget(parent->getWidget(), PG_Rect(x,y,w,h), "GradientWidget") {
  SetBackgroundBlend(0);
}

GamePanel::~GamePanel() {
}