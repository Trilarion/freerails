/*
 * $Id$
 */

#include "GameApplication.h"
#include "GameMainWindow.h"

#include <qapplication.h>
#include <qlabel.h>
#include <qpixmap.h>
#include <qstring.h>
#include <iostream.h>

GameApplication::GameApplication(int argc, char *argv[]) :
    BaseApplication(argc, argv)
{
  application = new QApplication(argc,argv);
  splash = 0l;
}

GameApplication::~GameApplication() {
  if(splash)
    delete splash;
}

int GameApplication::run() {
  return application->exec();
}

void GameApplication::showSplash()
{
  // Lots of code in this method is taken from Qt Designer's code
  splash = new QLabel(0, "Splash screen", Qt::WDestructiveClose |
      Qt::WStyle_Customize | Qt::WStyle_NoBorder | Qt::WX11BypassWM |
      Qt::WStyle_StaysOnTop);
  splash->setFrameStyle(QFrame::WinPanel | QFrame::Raised);
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
    splash->hide(); // delete maybe
}

void GameApplication::setMainWindow(GameMainWindow* mw)
{
  application->setMainWidget(mw->getWidget());
}
