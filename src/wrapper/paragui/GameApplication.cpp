/*
 * $Id$
 */

#include "GameApplication.h"

GameApplication::GameApplication(int argc, char *argv[]):BaseApplication(argc, argv) {

  char theme[20];
  strcpy(theme, "default");

  screenFlags = SDL_SWSURFACE;
  screenDepth = 0;

  for(int c=1; c<argc; c++) {
    if(argv[c][0] != '-') { strcpy(theme, argv[c]); }
    
    if(strcmp(argv[c], "-f") == 0) { screenFlags |= SDL_FULLSCREEN; }
    if(strcmp(argv[c], "-bpp") == 0) { screenDepth = atoi(argv[++c]); }
  }

  if (!(screenFlags&SDL_FULLSCREEN)) {
    screenFlags = SDL_SWSURFACE | SDL_HWPALETTE;
  }

  pGlobalApp = new PG_Application();
  pGlobalApp->LoadTheme(theme);
  pGlobalApp->InitScreen(800,600,screenDepth,screenFlags);
}

GameApplication::~GameApplication() {

    delete pGlobalApp;
}

int GameApplication::run() {

int result;
  showSplash();
  setCaption("FreeRails");
  sleep(1);
  hideSplash();
  while (1) {
    GameMainWindow mw( 0, 0, 800, 600);
    GameModeSelectDialog dialog(&mw, 250, 150, 300, 200, "Choose game mode");
    result=dialog.show();
    printf("Result=%i\n",result);
    if (result==-1) {
      pGlobalApp->Quit();
      return 0;
    }
    mapView=new GameMapView(&mw, 0, 0, 650, 450 , worldMap);
    netView=new GameNetView(&mw, 0, 450, 650, 150);
    panel=new GamePanel(&mw, 650, 0, 150, 600 /* ,WorldMap */);
    mapView->Show();
    netView->Show();
    panel->Show();
    pGlobalApp->Run();
    delete mapView;
    delete panel;
  }
}

bool GameApplication::initScreen(int x, int y, int w, int h) {
  return pGlobalApp->InitScreen(w,h,screenDepth,screenFlags);
}

void GameApplication::setCaption(const char *title) {
  pGlobalApp->SetCaption(title,NULL);
}

void GameApplication::showSplash() {
  splash = new PG_GradientWidget(NULL, PG_Rect(100,100,600,400));
  char file[]="data/graphics/ui/title.png";
  splash->SetBackground(file,BKMODE_STRETCH);
  splash->SetBackgroundBlend(0);
  splash->SetFadeSteps(20);
  splash->Show();
}

void GameApplication::hideSplash() {
  if(splash)
    splash->Hide();
}

void GameApplication::setMainWindow(GameMainWindow* mw) {

}