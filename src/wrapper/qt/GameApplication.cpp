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

GameApplication::GameApplication(int argc, char *argv[]) :
    BaseApplication(argc, argv)
{
  application = new QApplication(argc,argv);
  mW = 0l;
  splash = 0l;
  vertLayout = 0l;
  horLayout = 0l;
  //map = 0l;
}

GameApplication::~GameApplication() {
	if(splash) delete splash;

	if(horLayout) delete horLayout;
	if(vertLayout) delete vertLayout;
	//if(map) delete map;
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

	setMainWindow(mW);
	hideSplash();

	// Show dialog menu for game mode
	GameModeSelectDialog dialog(mW, 250, 150, 300, 200, "Choose game mode");
	printf("Result=%i\n", dialog.Show());	// TEMP

	// TEMP: Show the playfield (map, panel, menu like in RTII(tm))
	// Layout managers
	vertLayout = new QVBoxLayout(mW->getWidget());
	horLayout = new QHBoxLayout(vertLayout);

	// The map where the actual game is displayed.
	map = new GameMap(mW->getWidget(), "map");

	// A horizontal panel with a minimap, train info, ...
	panel = new GamePanel(mW, "panel");

	// A vertical menubar.
	menu = new GameMenuBar(mW, "menu");

	horLayout->addWidget(menu);
	horLayout->addWidget(map->getWidget());
	vertLayout->addWidget(panel);

	menu->Show();
	map->Show();
	panel->show();


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
    splash->hide(); // deleted splash (WDestructiveClose flag in splash!!)
}

void GameApplication::setMainWindow(GameMainWindow* mw)
{
  application->setMainWidget(mw->getWidget());
}
