/*
 * $Id$
 */

#include "GameApplication.h"

GameApplication::GameApplication(int argc, char *argv[]):BaseApplication(argc, argv) {
  application = new QApplication(argc,argv);
}

GameApplication::~GameApplication() {
  delete widget;
}

void GameApplication::run() {
  application->exec();
}

bool GameApplication::initScreen(int x, int y, int w, int h) {
  widget = new QWidget();
  widget->setGeometry(x,y,w,h);
  application->setMainWidget(widget);
  widget->show();
  return true;
}

void GameApplication::setCaption(const char *title) {
    widget->setCaption(title);
}