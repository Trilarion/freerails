/*
 * $Id$
 */

#include <iostream.h>
#include <qlayout.h>

#include "GameMainWindow.h"
#include "GameMenuBar.h"
#include "GameMap.h"
#include "GameMapView.h"
#include "GamePanel.h"

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

GameModeSelector::GameMode GameMainWindow::askGameMode()
{
  GameModeSelector::GameMode m;
  GameModeSelector* s = new GameModeSelector(this);
  m = s->exec();
  delete s;
  return m;
}

void GameMainWindow::constructPlayField()
{
  // Maybe such layout isn't best
  layout = new QVBoxLayout(widget);
  layout_h = new QHBoxLayout(layout);
  menubar = new GameMenuBar(this, "menubar");
  map = new GameMap(this, "map");
  mapview = new GameMapView(map, this, "mapview");
  panel = new GamePanel(this, "panel");

  layout_h->addWidget(menubar);
  layout_h->addWidget(mapview);
  layout->addWidget(panel);

  menubar->show();
  mapview->show();
  panel->show();
}
