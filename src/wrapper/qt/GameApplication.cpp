/*
 * $Id$
 */

#include <pthread.h>
#include <unistd.h>

#include <qapplication.h>
#include <qlabel.h>                               
#include <qpixmap.h>
#include <qstring.h>

#include "Engine.h"
#include "GameApplication.h"
#include "GameMainWindow.h"
#include "singlegameoptiondialog.h"

GameApplication::GameApplication(int argc, char *argv[]) :
    BaseApplication(argc, argv)
{
  application = new QApplication(argc,argv);
  mW = 0l;
  splash = 0l;
}

GameApplication::~GameApplication()
{
  if(splash)
    delete splash;
}

void* runEngine(void *data)
{
  #warning complete me
  GameApplication *object;
  object = static_cast<GameApplication*>(data);    
  qDebug("runEngine gestartet");
  int play_time = 0;
  qDebug("this in runEngine: 0x%08x", int(object));
  qDebug("status in runEngine: %i", int(object->getEngine()->getGameState()));
  while (object->getEngine()->getGameState() < Engine::Stopping)
  {
    object->retrieveMessage();
    object->getEngine()->checkNet();
    object->getEngine()->checkNext(play_time);
    usleep(10500);      // wait for 10.5 ms
    play_time += 11;
  }
  return 0;
}

int GameApplication::run()
{
  // Show the splash
  showSplash();
  application->processEvents();
  sleep(1);

  hideSplash();
  application->processEvents();

  while(1)
  {
    // FIXME: should be done in constructor with parameters from argc, argv
    mW = new GameMainWindow(0, 0, 800, 600);

    // FIXME: versionstring for caption
    mW->setCaption("Freerails");

    //setMainWindow(mW);
    application->setMainWidget((QWidget *)mW->getWidget());
    application->processEvents();
    // Show dialog menu for game mode
    GameModeSelector::GameMode mode = mW->askGameMode();
    qDebug("mW->askGameMode() result=%i\n", (int)mode);

    // Show mainwindow
    application->processEvents();

    // If user wants to quit, then quit
    if(mode == GameModeSelector::Quit)
    {
      application->exit(0);
      break;
    }

    #warning complete me
    QString tmp_name;
    int tmp_width, tmp_height;
    int status;
    SingleGameOptionDialog *sgoDlg = new SingleGameOptionDialog(mW);
    status = sgoDlg->exec();
    if (status == QDialog::Accepted)
    {
      tmp_name = sgoDlg->getName();
      tmp_width = sgoDlg->getWidth();
      tmp_height = sgoDlg->getHeight();
    }
    delete sgoDlg;

    if (status == QDialog::Accepted)
    {
      initSingleGame(tmp_name, tmp_width, tmp_height, 0);

      engine = new Engine(worldMap, playerSelf);
      CHECK_PTR(engine);


      // Construct playfield (map, panel, buttons)
      mW->setEngine(engine);
      mW->constructPlayField();

      pthread_t ttid_cmd;
      qDebug("this in run: 0x%08x", int(this));
      pthread_create(&ttid_cmd, NULL, &runEngine, (void*)this);
      pthread_detach(ttid_cmd);
      Engine::GameState state = Engine::Running;
      Message* msg = new Message(Message::stateOfGame, 0, &state);
      qDebug("sende Nachricht an Engine");
      engine->sendMsg(msg);
      qDebug("Nachricht gesendet");
      application->exec();
      delete mW;
      qDebug("Spiel beendet");
      state = Engine::Stopping;
      msg = new Message(Message::stateOfGame, 0, &state);
      engine->sendMsg(msg);
      pthread_cancel(ttid_cmd);
    }
  }
  return 1;
}

void GameApplication::showSplash()
{
  // Lots of code in this method is taken from Qt Designer's code
  splash = new QLabel(0, "Splash screen", Qt::WDestructiveClose |
      Qt::WStyle_Customize | Qt::WStyle_NoBorder | Qt::WX11BypassWM |
      Qt::WStyle_StaysOnTop);
  splash->setFrameStyle(QFrame::WinPanel | QFrame::Raised);
  // New, better and probably a bit smaller splashscreen picture wanted!
  splash->setPixmap(QPixmap(QString("data/graphics/ui/title.png")));
  splash->adjustSize();
  splash->move((QApplication::desktop()->width() - splash->width()) / 2,
      (QApplication::desktop()->height() - splash->height()) / 2);
  splash->show();
  splash->repaint(false);
  QApplication::flushX();
}

void GameApplication::hideSplash()
{
  if(splash)
    splash->hide(); // deleted splash (WDestructiveClose flag in splash!!)
}

// get messages from engine
void GameApplication::retrieveMessage()
{
  Message *msg;

//  qDebug("in retrieveMessage");  
  while(engine->haveMsg())
  {
    msg = engine->getMsg();
    qDebug("MsgType is %d", int(msg->getMsgType()));
    qDebug("MsgID is %ld", msg->getMsgID());
  }
}
