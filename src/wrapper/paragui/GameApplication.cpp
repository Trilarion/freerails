/*
 * $Id$
 */

#include "GameApplication.h"

#include <SDL_thread.h>

#include "Client.h"
#include "Message.h"

GameApplication::GameApplication(int argc, char *argv[]):BaseApplication(argc, argv) {

  char theme[20];
  std::string replay;

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

  cerr << "networking" << endl;

  /*
  Client* client=new Client();
  client->open("localhost",30000);
  */
/*  *client << "Testmessage";
  
  *client >> replay;
*/
  cerr << replay << endl;
  
  mapView=NULL;
  netView=NULL;
  panel=NULL;
}

GameApplication::~GameApplication() {

    delete pGlobalApp;
}

int GameApplication::runEngine(void* data)
{

  GameApplication* object = static_cast<GameApplication*>(data);
  while (object->guiEngine->getGameState()<GuiEngine::Stopping) {
    object->guiEngine->checkNet();
    object->guiEngine->checkNext(SDL_GetTicks());
    SDL_Delay(10);
  }
  
  return 0;
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
    cout << "Result=" << result << endl;
    /* printf("Result=%i\n",result); */
    if (result==-1) {  // Quit
      pGlobalApp->Quit();
      return 0;
    }
    GameDataSelectDialog dataDialog(&mw, 200, 100, 400, 300, "Choos game Data", result);
    result=dataDialog.show();
    cout << "Result=" << result << endl;
    // get Player Name
    if (result==1) {  // Single Player
      initSingleGame(dataDialog.getName(), dataDialog.getWidth(), dataDialog.getHeight(), 0);
      guiEngine=new GuiEngine(playerSelf, dataDialog.getWidth(), dataDialog.getHeight());
      mapView=new GameMapView(&mw, 0, 0, 650, 600 , guiEngine);
      panel=new GamePanel(&mw, 650, 0, 150, 600, guiEngine, mapView);
      mapView->Show();
      panel->Show();
    } else
    if (result==2) {  
      // Multi Player Server
      // TODO
      // get Network settings
      initServerGame(dataDialog.getName(), dataDialog.getWidth(), dataDialog.getHeight(), 0);
      /* Server* server=new Server(30000); */
      
      // start engine Server
      guiEngine=new GuiEngine(playerSelf, dataDialog.getWidth(), dataDialog.getHeight(), 30000);
      
      mapView=new GameMapView(&mw, 0, 0, 650, 450 , guiEngine);
      panel=new GamePanel(&mw, 650, 0, 150, 600, guiEngine, mapView);
      netView=new GameNetView(&mw, 0, 450, 650, 150);
      
      mapView->Show();
      panel->Show();
      netView->Show();
    } else
    if (result==3) {
      // Multi Player Client
      initClientGame(dataDialog.getName());

      /*    Need's change to client
	    Server* server=new Server(30000);
	    engine=new Engine(playerSelf, server);
      */
      /*
	Client* client=new Client();
      */
      /* client->open(host, port);*/
      /* Start engine client */

      guiEngine=new GuiEngine(playerSelf, dataDialog.getWidth(), dataDialog.getHeight(), 
			      dataDialog.getIpAddress(), dataDialog.getPort());

            
      mapView=new GameMapView(&mw, 0, 0, 650, 450 , guiEngine);
      panel=new GamePanel(&mw, 650, 0, 150, 600, guiEngine, mapView);
      netView=new GameNetView(&mw, 0, 450, 650, 150);
      
      cout << " Why not..." << endl;
      
      mapView->Show();
      panel->Show();
      netView->Show();
      
      cout << " WHAAA" << endl;
    }
    if (result>0)
    {
      SDL_Thread* thread2 = SDL_CreateThread(GameApplication::runEngine, this);

      guiEngine->changeGameState(GuiEngine::Running);
      
      pGlobalApp->Run();
      
      guiEngine->changeGameState(GuiEngine::Stopping);
      
      SDL_WaitThread(thread2, NULL);
      if (guiEngine!=NULL) { delete guiEngine; guiEngine=NULL; }
      if (mapView!=NULL) { delete mapView; mapView=NULL; }
      if (netView!=NULL) { delete netView; netView=NULL; }
      if (panel!=NULL) { delete panel; panel=NULL; }
    }
  }
}

bool GameApplication::initScreen(int x, int y, int w, int h) {
  return pGlobalApp->InitScreen(w,h,screenDepth,screenFlags);
}

void GameApplication::setCaption(const char *title) {
  pGlobalApp->SetCaption(title,NULL);
}

void GameApplication::showSplash() {
  splash = new PG_ThemeWidget(NULL, PG_Rect(100,100,600,400));
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
