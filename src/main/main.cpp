#include "game.h"

void Splash(MyGameApplication* app) {
  GameWidget splash(app, 100,100,600,400);
  char file[]="data/graphics/ui/title.png";
/*  splash.SetBackground(file,BKMODE_STRETCH);
  splash.SetBackgroundBlend(0);*/
  splash.show();
  app->initGame();
//  SDL_Delay(2000);
  splash.hide();
}

int main(int argc, char *argv[])
{
  char versionString[50];

  strcpy(versionString,"FreeRails v"GAME_VERSION"");

  // construct the application object
  MyGameApplication app(argc,argv);

  if(!app.initScreen(0,0,800,600)){
    printf("Resolution not supported\n");
    exit(-1);
  }

  app.setCaption(versionString);

  Splash(&app);
  
  app.askUser();    
  app.run();

  return EXIT_SUCCESS;
}
