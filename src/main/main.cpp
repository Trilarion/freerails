#include "game.h"
#include "BaseApplication.h"

//#include <pgwidgetlist.h>

void Splash(GameApplication* app) {
/*  PG_GradientWidget splash(NULL, PG_Rect(100,100,600,400));
  char file[]="data/graphics/ui/title.png";
  splash.SetBackground(file,BKMODE_STRETCH);
  splash.SetBackgroundBlend(0);
  splash.Show();
  app->InitGame();
  SDL_Delay(2000);
  splash.Hide();*/
}

int main(int argc, char *argv[])
{
  char versionString[50];

  strcpy(versionString,"FreeRails v"GAME_VERSION"");

  // construct the application object
  MyGameApplication app(argc,argv);

  if(!app.InitScreen(0,0,800,600)){
    printf("Resolution not supported\n");
    exit(-1);
  }

  app.SetCaption(versionString);

  Splash(&app);
  
  app.AskUser();    
  app.Run();

  return EXIT_SUCCESS;
}
