/*
 * $Id$
 */

#include "GameWidget.h"

GameWidget::GameWidget() {
}

GameWidget::GameWidget(GameWidget* parent, int x, int y, int w, int h) {
  widget= new QWidget(parent->getWidget());
  widget->setGeometry(x,y,w,h);
}

GameWidget::GameWidget(GameMainWindow* parent, int x, int y, int w, int h) {
  widget= new QWidget(parent->getWidget());
  widget->setGeometry(x,y,w,h);
}

GameWidget::~GameWidget() {

}

void GameWidget::show() {
  widget->show();
}

void GameWidget::hide() {
  widget->hide();
}
