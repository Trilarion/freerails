/** $Id$
  * Panel (minimap, train info, ...) class
  */

#include "GamePanel.h"

GamePanel::GamePanel(QWidget* parent, const char* name)
         : QWidget(parent, name) {
  setFixedHeight(50);
  but = new QPushButton("T", this);
  but->show();
}

GamePanel::GamePanel(GameMainWindow* parent, const char* name)
         : QWidget(parent->getWidget(), name) {
  setFixedHeight(50);
  but = new QPushButton("T", this);
  but->show();
}

GamePanel::~GamePanel() {
}