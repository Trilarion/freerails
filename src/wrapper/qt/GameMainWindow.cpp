/*
 * $Id$
 */

#include "GameMainWindow.h"
#include "GameMenuBar.h"
#include "GameMap.h"
#include "GameMapView.h"
#include "GamePanel.h"
#include "GameWidget.h"

GameMainWindow::GameMainWindow(int x, int y, int w, int h) :
    BaseMainWindow(x, y, w, h)
{
  mapview = NULL;
  panel = NULL;
  
  widget = new GameWidget(this);
  widget->setGeometry(x, y, w, h);
  widget->show();
}

GameMainWindow::~GameMainWindow()
{
  if(widget)
    delete widget;
}

void GameMainWindow::setCaption(const char* caption)
{
  widget->setCaption(caption);
}

void GameMainWindow::resize(int w, int h)
{
  qDebug("(GameMainWindow) neue Gr��e %dx%d", w, h);
  if (mapview)
    mapview->resize(w - 176, h);
  if(panel)
  {
    panel->resize(176, h);
    panel->move(w - 176, 0);
  }
}

void GameMainWindow::setGuiEngine(GuiEngine *_guiEngine)
{
  guiEngine = _guiEngine;
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
  map = new GameMap(guiEngine, this);
  CHECK_PTR(map);
  
  mapview = new GameMapView(guiEngine, map, this);
  CHECK_PTR(mapview);

  panel = new GamePanel(guiEngine, mapview, this);
  CHECK_PTR(panel);
  panel->move(widget->width() - 176, 0);

  mapview->show();
  panel->show();
}

void GameMainWindow::exitGame()
{
  qDebug("quit current game");
  delete widget;
  widget = 0;
}
