/*
 * $Id$
 */

#include "GameMainWindow.h"

GameMainWindow::GameMainWindow(int x, int y, int w, int h, PG_FrameApplication* _app) :
    BaseMainWindow(x, y, w, h)
{
  widget = new PG_Widget(NULL, PG_Rect(x, y, w, h));
  widget->Show();
  app = _app;
}

GameMainWindow::~GameMainWindow()
{
  delete widget;
}

void GameMainWindow::setCaption(const char* caption)
{
}
