/*
 * $Id$
 */

#include "game.h"
#include "i18n.h"
#include <stdio.h>
#include <unistd.h>

int main(int argc, char *argv[])
{
  i18n_init();

  char versionString[50];

  sprintf(versionString, _("FreeRails v%s"), PACKAGE_VERSION);

  // construct the application object
  MyGameApplication app(argc,argv);

  return app.run();
}
