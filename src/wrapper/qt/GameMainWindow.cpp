/*
 * $Id$
 */

#include "GameMainWindow.h"

#include <iostream.h>

GameMainWindow::GameMainWindow(int x, int y, int w, int h) :
    BaseMainWindow(x, y, w, h)
{
  widget = new QWidget(0, "MainWindow");
  widget->setGeometry(x, y, w, h);
  widget->show();
}

GameMainWindow::~GameMainWindow()
{
  delete widget;
}

void GameMainWindow::setCaption(const char* caption)
{
  widget->setCaption(caption);
}
