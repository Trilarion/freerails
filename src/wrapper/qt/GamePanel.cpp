/** $Id$
  * Panel (minimap, train info, ...) class
  */

#include <qiconset.h>
#include <qtoolbutton.h>

#include "Engine.h"
#include "GameMainWindow.h"
#include "GameMapView.h"
#include "GamePanel.h"

GamePanel::GamePanel(Engine *_engine, GameMapView *_mapView, GameMainWindow* parent, const char* name) : QWidget(parent->getWidget(), name)
{
  engine = _engine;
  mapView = _mapView;
  
  resize(150, parent->getWidget()->height());
  setFixedWidth(150);

  setupButtons();
}

GamePanel::~GamePanel()
{
}

void GamePanel::setupButtons()
{
  QIconSet iconSet_t;
  QIconSet iconSet_s;

  iconSet_t.setIconSize(QIconSet::Small, QSize(25,25));
  iconSet_s.setIconSize(QIconSet::Small, QSize(25,25));
  
  iconSet_t.setPixmap("/usr/local/share/freerails/build_track_down.png", QIconSet::Small, QIconSet::Normal, QIconSet::On);
  iconSet_t.setPixmap("/usr/local/share/freerails/build_track_up.png", QIconSet::Small, QIconSet::Normal, QIconSet::Off);
  iconSet_s.setPixmap("/usr/local/share/freerails/build_station_down.png", QIconSet::Small, QIconSet::Normal, QIconSet::On);
  iconSet_s.setPixmap("/usr/local/share/freerails/build_station_up.png", QIconSet::Small, QIconSet::Normal, QIconSet::Off);

  btnTrack = new QToolButton(this);
  CHECK_PTR(btnTrack);
  btnTrack->resize(25, 25);
  btnTrack->setToggleButton(true);
  btnTrack->setIconSet(iconSet_t);
  btnTrack->move(5, 400);
  btnTrack->setOn(false);

  btnStation = new QToolButton(this);
  CHECK_PTR(btnStation);
  btnStation->resize(25, 25);
  btnStation->setToggleButton(true);
  btnStation->setIconSet(iconSet_s);
  btnStation->move(35,400);
  btnStation->setOn(false);

  btnPause = new QToolButton(this);
  CHECK_PTR(btnPause);
  btnPause->resize(125, 25);
  btnPause->setText("Pause");
  btnPause->move(5, 430);

  btnExit = new QToolButton(this);
  CHECK_PTR(btnExit);
  btnExit->resize(125, 25);
  btnExit->setText("Exit");
  btnExit->move(5, 460);

  connect(btnTrack, SIGNAL(clicked()), SLOT(handler_track()));
  connect(btnStation, SIGNAL(clicked()), SLOT(handler_station()));
  connect(btnPause, SIGNAL(clicked()), SLOT(handler_pause()));
  connect(btnExit, SIGNAL(clicked()), SLOT(handler_exit()));
}

void GamePanel::handler_pause()
{
  Engine::GameState state = Engine::Pausing;
  Message *msg = new Message(Message::stateOfGame, 0, &state);
  engine->sendMsg(msg);
}

void GamePanel::handler_track()
{
  bool status = btnTrack->isOn();
  releaseAllButtons();
  if(status)
  {
    mapView->setMouseType(GameMapView::buildTrack);
    btnTrack->setOn(true);
  }
}

void GamePanel::handler_station()
{
  bool status = btnStation->isOn();
  releaseAllButtons();
  if(status)
  {
    mapView->setMouseType(GameMapView::buildStation);
    btnStation->setOn(true);
  }
}

void GamePanel::handler_exit()
{
  #warning complete me
  exit(0);
}

void GamePanel::releaseAllButtons()
{
  btnStation->setOn(false);
  btnTrack->setOn(false);
  mapView->setMouseType(GameMapView::normal);
}
