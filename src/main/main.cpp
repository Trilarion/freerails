/*
 * $Id$
 */

#include "game.h"
#include "i18n.h"
#include "GameMainWindow.h"
#include "GameModeSelectDialog.h"
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
  sleep(1);

  // No error checking yet
  // Maybe this should be done BEFORE app.initGame()?
  GameMainWindow mw(0, 0, 800, 600);
  mw.setCaption(versionString);
  app.setMainWindow(&mw);

  app.hideSplash();


  // Ask's for user choose
  // Must insert in GameApplication, only for testing/demonstration here.
  GameModeSelectDialog dialog(&mw, 250,150,300,200, "Choose game mode");

  printf("Result=%i\n", dialog.Show());

  return app.run();;
}
