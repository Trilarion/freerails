/*
 * $Id$
 */

#include "GameApplication.h"

#include "Client.h"

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

  pGlobalApp = new PG_Application2();
  pGlobalApp->LoadTheme(theme);
  pGlobalApp->InitScreen(800,600,screenDepth,screenFlags);
  pGlobalApp->EnableAppIdleCalls(true);

  cerr << "networking" << endl;

  Client* client=new Client();
  client->connect("localhost",9999);
  client->send(theme,strlen(theme));
  
  mapView=NULL;
  netView=NULL;
  panel=NULL;
}

GameApplication::~GameApplication() {

    delete pGlobalApp;
}

void PG_Application2::eventIdle()
{
  engine->checkNext(SDL_GetTicks());
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
    if (result==-1) {  // Quit
      pGlobalApp->Quit();
      return 0;
    }
    // Ask for Playername
    // get Player Name
    if (result==1) {  // Single Player
      initSingleGame();
      engine=new Engine(worldMap, playerSelf);
      mapView=new GameMapView(&mw, 0, 0, 650, 600 , worldMap);
      panel=new GamePanel(&mw, 650, 0, 150, 600 /* ,WorldMap */);
      mapView->Show();
      panel->Show();
    }
    if (result==2) {  // Multi Player
      // show modal Network dialog
      // get Network settings
      // get Network/Clientsocket
      // initServerGame() or initClientGame()
      // start engine Client or Server
      // engine=new Engine(worldMap);

      mapView=new GameMapView(&mw, 0, 0, 650, 450 , worldMap);
      panel=new GamePanel(&mw, 650, 0, 150, 600 /* ,WorldMap */);
      netView=new GameNetView(&mw, 0, 450, 650, 150);

      mapView->Show();
      panel->Show();
      netView->Show();
    }
    pGlobalApp->setEngine(engine);
    engine->startGame();
    pGlobalApp->Run();
    if (engine!=NULL) { delete engine; engine=NULL; }
    if (mapView!=NULL) { delete mapView; engine=NULL; }
    if (netView!=NULL) { delete netView; engine=NULL; }
    if (panel!=NULL) { delete panel; engine=NULL; }
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