/*
 * $Id$
 */

#include <qwidget.h>

#include "GameMainWindow.h"
#include "GameMenuBar.h"
#include "GameMap.h"
#include "GameMapView.h"
#include "GamePanel.h"

GameMainWindow::GameMainWindow(int x, int y, int w, int h) :
    BaseMainWindow(x, y, w, h)
{
  widget = new QWidget(0, "MainWindow");
  CHECK_PTR(widget);
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

void GameMainWindow::setEngine(Engine *_engine)
{
  engine = _engine;
}

GameModeSelector::GameMode GameMainWindow::askGameMode()
{
  GameModeSelector::GameMode m;
  GameModeSelector* s = new GameModeSelector(this);
  m = GameModeSelector::GameMode(s->exec());
  delete s;
  return m;
}

void GameMainWindow::constructPlayField()
{
  map = new GameMap(engine, this);
  CHECK_PTR(map);
  
  mapview = new GameMapView(engine, map, this);
  CHECK_PTR(mapview);

  panel = new GamePanel(engine, mapview, this);
  CHECK_PTR(panel);
  panel->move(widget->width() - 176, 0);

  mapview->show();
  panel->show();
}

//void GameMainWindow::resizeEvent(QResizeEvent *)
//{
//  map->resize(widget()->width() - 176, widget()->height());
//  panel->move(widget->width() - 176, 0);
//}
