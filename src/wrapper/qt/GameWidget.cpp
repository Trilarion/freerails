/*
 * $Id$
 */

#include "GameWidget.h"

GameWidget::GameWidget(GameMainWindow* _parent, const char *name) :
  BaseWidget(), QWidget(0, name)
{
  parent = _parent;
}

GameWidget::~GameWidget()
{

}

void GameWidget::show()
{
  QWidget::show();
}

void GameWidget::hide()
{
  QWidget::hide();
}

void GameWidget::setGeometry(int x, int y, int w, int h)
{
  QWidget::setGeometry(x, y, w, h);
}

void GameWidget::setCaption(const char *name)
{
  QWidget::setCaption(name);
}

void GameWidget::resizeEvent(QResizeEvent *e)
{
  qDebug("(GameWidget) neue Größe %dx%d", e->size().width(), e->size().height());
  int w, h;
  w = e->size().width();
  h = e->size().height();
  if ((w < 640) || (h < 480))
  {
    if (w < 640)
      w = 640;
    if (h < 480)
      h = 480;
    resize(w, h);
  }
  else
  {
    if (parent)
    {
      parent->resize(w, h);
    }
  }
}
