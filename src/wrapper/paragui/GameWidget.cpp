/*
 * $Id$
 */

#include "GameWidget.h"

GameWidget::GameWidget() {

}

GameWidget::GameWidget(GameWidget* parent, int x, int y, int w, int h) {
    widget= new PG_Widget(parent->getWidget(),PG_Rect(x,y,w,h));
}

GameWidget::GameWidget(GameApplication* parent, int x, int y, int w, int h) {
  widget= new PG_Widget(NULL,PG_Rect(x,y,w,h));
}

GameWidget::~GameWidget() {

}

void GameWidget::show() {
  widget->Show();
}

void GameWidget::hide() {
  widget->Hide();
}
