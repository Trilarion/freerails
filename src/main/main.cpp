/*
 * $Id$
 */

#include "game.h"
#include "i18n.h"
#include "GameMainWindow.h"
#include <stdio.h>
#include <unistd.h>

int main(int argc, char *argv[])
{
  i18n_init();

  char versionString[50];

  sprintf(versionString, _("FreeRails v%s"), GAME_VERSION);

  // construct the application object
  MyGameApplication app(argc,argv);

  // Show splash screen
  app.showSplash();
  app.initGame();
  // To show splash for a while :-)
  // Remove when initialization works
  sleep(3);

  // No error checking yet
  // Maybe this should be done BEFORE app.initGame()?
  GameMainWindow mw(0, 0, 800, 600);
  mw.setCaption(versionString);
  app.setMainWindow(&mw);

  app.hideSplash();

  // What is this for ?
  //GameDialog dialog(&mw, 250,150,300,300, "Spielart wählen");
  //dialog.show();

  printf("choose\n");

  return app.run();
}
