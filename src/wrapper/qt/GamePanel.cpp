/** $Id$
  * Panel (minimap, train info, ...) class
  */

#include <qiconset.h>
#include <qtoolbutton.h>
#include <qwidgetstack.h>

#include "Engine.h"
#include "GameMainWindow.h"
#include "GameMapView.h"
#include "GamePanel.h"

GamePanel::GamePanel(Engine *_engine, GameMapView *_mapView, GameMainWindow* parent, const char* name) : QWidget(parent->getWidget(), name)
{
  engine = _engine;
  mapView = _mapView;
  
  resize(176, parent->getWidget()->height());
  setFixedWidth(176);

  setupWdgMessages();
  setupWstTrainBuild();
  setupButtons();
}

GamePanel::~GamePanel()
{
}

void GamePanel::setupWdgMessages()
{
  wdgMessages = new QWidget(this);
  CHECK_PTR(wdgMessages);
  wdgMessages->resize(176, 144);
  wdgMessages->setBackgroundMode(Qt::FixedPixmap);
  wdgMessages->setBackgroundPixmap(QPixmap("/usr/local/share/freerails/panel.png"));
  wdgMessages->move(0, height() - 444);
}

void GamePanel::setupButtons()
{
  QIconSet iconSet_tt;
  QIconSet iconSet_tb;

  iconSet_tt.setIconSize(QIconSet::Small, QSize(88,50));
  iconSet_tb.setIconSize(QIconSet::Small, QSize(88,50));

  QPixmap pixmap, pix_t;
  pix_t.resize(88, 50);
  pixmap.load("/usr/local/share/freerails/panel_build_on.png");
  bitBlt(&pix_t, 0, 0, &pixmap, 0, 0, 88, 50);
  iconSet_tt.setPixmap(pix_t, QIconSet::Small, QIconSet::Normal, QIconSet::Off);
  bitBlt(&pix_t, 0, 0, &pixmap, 88, 0, 88, 50);
  iconSet_tb.setPixmap(pix_t, QIconSet::Small, QIconSet::Normal, QIconSet::On);
  pixmap.load("/usr/local/share/freerails/panel_train_on.png");
  bitBlt(&pix_t, 0, 0, &pixmap, 0, 0, 88, 50);
  iconSet_tt.setPixmap(pix_t, QIconSet::Small, QIconSet::Normal, QIconSet::On);
  bitBlt(&pix_t, 0, 0, &pixmap, 88, 0, 88, 50);
  iconSet_tb.setPixmap(pix_t, QIconSet::Small, QIconSet::Normal, QIconSet::Off);

  btnTabTrain = new QToolButton(this);
  CHECK_PTR(btnTabTrain);
  btnTabTrain->resize(88, 50);
  btnTabTrain->setToggleButton(true);
  btnTabTrain->setIconSet(iconSet_tt);
  btnTabTrain->move(0, height() - 300);
  btnTabTrain->setOn(false);

  btnTabBuild = new QToolButton(this);
  CHECK_PTR(btnTabBuild);
  btnTabBuild->resize(88, 50);
  btnTabBuild->setToggleButton(true);
  btnTabBuild->setIconSet(iconSet_tb);
  btnTabBuild->move(88, height() - 300);
  btnTabBuild->setOn(true);
  build_is_on = true;
  wstTrainBuild->raiseWidget(2);

  connect(btnTabTrain, SIGNAL(clicked()), SLOT(slotTabTrain()));
  connect(btnTabBuild, SIGNAL(clicked()), SLOT(slotTabBuild()));  
}

void GamePanel::setupWstTrainBuild()
{
  wstTrainBuild = new QWidgetStack(this);
  CHECK_PTR(wstTrainBuild);
  wstTrainBuild->setFixedSize(176, 250);

  setupPanelTrain();
  setupPanelBuild();
  wstTrainBuild->addWidget(panelTrain, 1);
  wstTrainBuild->addWidget(panelBuild, 2);

  wstTrainBuild->move(0, height() - 250);
}

void GamePanel::setupPanelTrain()
{
  panelTrain = new QWidget(wstTrainBuild);
  CHECK_PTR(panelTrain);
  panelTrain->setFixedSize(176, 250);
  panelTrain->setBackgroundMode(Qt::FixedPixmap);
  panelTrain->setBackgroundPixmap(QPixmap("/usr/local/share/freerails/panel_train.png"));
}

void GamePanel::setupPanelBuild()
{
  panelBuild = new QWidget(wstTrainBuild);
  CHECK_PTR(panelBuild);
  panelBuild->setFixedSize(176, 250);
  panelBuild->setBackgroundMode(Qt::FixedPixmap);
  panelBuild->setBackgroundPixmap(QPixmap("/usr/local/share/freerails/panel_train.png"));

  QIconSet iconSet_t;
  QIconSet iconSet_s;

  iconSet_t.setIconSize(QIconSet::Small, QSize(25,25));
  iconSet_s.setIconSize(QIconSet::Small, QSize(25,25));

  iconSet_t.setPixmap("/usr/local/share/freerails/build_track_down.png", QIconSet::Small, QIconSet::Normal, QIconSet::On);
  iconSet_t.setPixmap("/usr/local/share/freerails/build_track_up.png", QIconSet::Small, QIconSet::Normal, QIconSet::Off);
  iconSet_s.setPixmap("/usr/local/share/freerails/build_station_down.png", QIconSet::Small, QIconSet::Normal, QIconSet::On);
  iconSet_s.setPixmap("/usr/local/share/freerails/build_station_up.png", QIconSet::Small, QIconSet::Normal, QIconSet::Off);

  btnTrack = new QToolButton(panelBuild);
  CHECK_PTR(btnTrack);
  btnTrack->resize(25, 25);
  btnTrack->setToggleButton(true);
  btnTrack->setIconSet(iconSet_t);
  btnTrack->move(5, 5);
  btnTrack->setOn(false);

  btnStation = new QToolButton(panelBuild);
  CHECK_PTR(btnStation);
  btnStation->resize(25, 25);
  btnStation->setToggleButton(true);
  btnStation->setIconSet(iconSet_s);
  btnStation->move(35, 5);
  btnStation->setOn(false);

  btnPause = new QToolButton(panelBuild);
  CHECK_PTR(btnPause);
  btnPause->resize(125, 25);
  btnPause->setText("Pause");
  btnPause->move(5, 190);

  btnExit = new QToolButton(panelBuild);
  CHECK_PTR(btnExit);
  btnExit->resize(125, 25);
  btnExit->setText("Exit");
  btnExit->move(5, 220);

  connect(btnTrack, SIGNAL(clicked()), SLOT(handler_track()));
  connect(btnStation, SIGNAL(clicked()), SLOT(handler_station()));
  connect(btnPause, SIGNAL(clicked()), SLOT(handler_pause()));
  connect(btnExit, SIGNAL(clicked()), SLOT(handler_exit()));
}

void GamePanel::slotTabTrain()
{
  if(!build_is_on)
  {
    btnTabTrain->setOn(true);
  }
  else
  {
    btnTabTrain->setOn(true);
    btnTabBuild->setOn(false);
    build_is_on = false;
    releaseAllButtons();
    wstTrainBuild->raiseWidget(1);
  }
}

void GamePanel::slotTabBuild()
{
  if(build_is_on)
  {
    btnTabBuild->setOn(true);
  }
  else
  {
    btnTabTrain->setOn(false);
    btnTabBuild->setOn(true);
    build_is_on = true;
    wstTrainBuild->raiseWidget(2);
  }
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
