/*
 * $Id$
 */

#include "GameWidget.h"

GameWidget::GameWidget(int x, int y, int w, int h, GameMainWindow* parent, const char *name) :
  BaseWidget()
{
  widget = new QWidget(parent->getWidget(), name);
  CHECK_PTR(widget);
  widget->setGeometry(x,y,w,h);
}

GameWidget::~GameWidget()
{

}

void GameWidget::show()
{
  widget->show();
}

void GameWidget::hide()
{
  widget->hide();
}

void GameWidget::resizeEvent(QResizeEvent *e)
{
//  emit(changeSize(e);
}
