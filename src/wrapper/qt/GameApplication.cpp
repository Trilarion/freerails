/*
 * $Id$
 */

#include "GameApplication.h"
#include "GameMainWindow.h"
#include "GameModeSelectDialog.h"

#include <qapplication.h>
#include <qlabel.h>
#include <qpixmap.h>
#include <qstring.h>

#include <stdio.h>
#include <unistd.h>
#include <iostream.h>

GameApplication::GameApplication(int argc, char *argv[]) :
    BaseApplication(argc, argv)
{
  application = new QApplication(argc,argv);
  mW = 0l;
  splash = 0l;
}

GameApplication::~GameApplication() {
  if(splash) delete splash;
}

int GameApplication::run() {
  // Show the splash
  showSplash();
  // FIXME: better method for sleep?
  sleep(1);

  // FIXME: should be done in constructor with parameters from argc, argv
  mW = new GameMainWindow(0, 0, 800, 600);

  // FIXME: versionstring for caption
  mW->setCaption("Freerails");

  //setMainWindow(mW);
  application->setMainWidget(mW->getWidget());
  application->processEvents();
  hideSplash();

  // Show dialog menu for game mode
  GameModeSelector::GameMode mode = mW->askGameMode();
  printf("mW->askGameMode() result=%i\n", (int)mode);

  // Show mainwindow
  application->processEvents();

  // If user wants to quit, then quit
  if(mode == GameModeSelector::Quit)
  {
    // Is this safe?
    exit(0);
  }

  // Some sort of 'multi or single game mode' test should be here
 
  // Construct playfield (map, panel, buttons)
  mW->constructPlayField();
  
  return application->exec();
}

void GameApplication::showSplash()
{
  // Lots of code in this method is taken from Qt Designer's code
  splash = new QLabel(0, "Splash screen", Qt::WDestructiveClose |
      Qt::WStyle_Customize | Qt::WStyle_NoBorder | Qt::WX11BypassWM |
      Qt::WStyle_StaysOnTop);
  splash->setFrameStyle(QFrame::WinPanel | QFrame::Raised);
  // New, better and probably a bit smaller splashscreen picture wanted!
  splash->setPixmap(QPixmap(QString("/usr/local/share/freerails/title.png")));
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

// Obsolote, because we call directly application->setMainWidget()
// It crashed, too, but don't know why
void GameApplication::setMainWindow(GameMainWindow* mw)
{
  application->setMainWidget(mw->getWidget());
}
