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

  screenFlags = SDL_SWSURFACE|SDL_DOUBLEBUF|SDL_ANYFORMAT;
  screenDepth = 16;

  for(int c=1; c<argc; c++) {
    if(argv[c][0] != '-') { strcpy(theme, argv[c]); }
    
    if(strcmp(argv[c], "-f") == 0) { screenFlags |= SDL_FULLSCREEN; }
    if(strcmp(argv[c], "-bpp") == 0) { screenDepth = atoi(argv[++c]); }
  }

  pGlobalApp = new PG_FrameApplication();
  pGlobalApp->LoadTheme(theme);
  pGlobalApp->InitScreen(800,600,screenDepth,screenFlags);
  
  std::cerr << "networking" << std::endl;

  /*
  Client* client=new Client();
  client->open("localhost",30000);
  */
/*  *client << "Testmessage";
  
  *client >> replay;
*/
  std::cerr << replay << std::endl;
  
  netView=NULL;
  panel=NULL;
}

GameApplication::~GameApplication() {

    delete pGlobalApp;
}

int GameApplication::runEngine(void* data)
{

  GameApplication* object = static_cast<GameApplication*>(data);
  while (object->guiEngine->getGameState() < GuiEngine::Stopping) {
    object->guiEngine->checkNet();
    object->guiEngine->checkNext(SDL_GetTicks());
    SDL_Delay(10);
  }
  
  return 0;
}

int GameApplication::run() {

  int result;
  char file[]="data/graphics/splash_screen.jpg";
  pGlobalApp->SetBackground(file);
  
  showSplash();
  setCaption("FreeRails");
  sleep(1);
  hideSplash();
  while (1) {
    GameMainWindow mw( 0, 0, 800, 600, pGlobalApp);
    GameModeSelectDialog dialog(&mw, 250, 150, 300, 200, "Choose game mode");
    result=dialog.show();
    std::cout << "Result=" << result << std::endl;
    if (result==-1) {  // Quit
      pGlobalApp->Quit();
      return 0;
    }
    GameDataSelectDialog dataDialog(&mw, 200, 100, 400, 300, "Choos game Data", result);
    result=dataDialog.show();
    std::cout << "Result=" << result << std::endl;
    // get Player Name
    if (result==1) {  // Single Player
      initSingleGame(dataDialog.getName(), dataDialog.getWidth(), dataDialog.getHeight(), 0);
      guiEngine=new GuiEngine(playerSelf, dataDialog.getWidth(), dataDialog.getHeight());
    } else
    if (result==2) {  
      // Multi Player Server
      // TODO
      // get Network settings
      initServerGame(dataDialog.getName(), dataDialog.getWidth(), dataDialog.getHeight(), 0);
      /* Server* server=new Server(30000); */
      
      // start engine Server
      guiEngine=new GuiEngine(playerSelf, dataDialog.getWidth(), dataDialog.getHeight(), 30000);

      netView=new GameNetView(&mw, 0, 450, 650, 150);
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

            
      netView=new GameNetView(&mw, 0, 450, 650, 150);
      
      std::cout << " Why not..." << std::endl;

      netView->Show();
      
      std::cout << " WHAAA" << std::endl;
    }
    if (result>0)
    {
      SDL_Thread* thread2 = SDL_CreateThread(GameApplication::runEngine, this);

      pGlobalApp->SetFrameHandler(new GameFrameHandler(pGlobalApp, guiEngine->getWorldMap()));
      mw.getWidget()->setGuiEngine(guiEngine);
      panel=new GamePanel(&mw, 650, 0, 150, 600, guiEngine);
      pGlobalApp->SetNetHandler(new GameNetHandler(NULL, guiEngine, panel, NULL));
      pGlobalApp->SetFPSLabel(new PG_Label(panel, PG_Rect(0,0,120,20), "FPS"));
      panel->Show();

      guiEngine->changeGameState(GuiEngine::Running);
      pGlobalApp->Run();
      guiEngine->changeGameState(GuiEngine::Stopping);
      
      SDL_WaitThread(thread2, NULL);
      if (guiEngine!=NULL) { delete guiEngine; guiEngine=NULL; }
      if (netView!=NULL) { delete netView; netView=NULL; }
      if (panel!=NULL) { delete panel; panel=NULL; }
    }
  }
}

bool GameApplication::initScreen(int x, int y, int w, int h) {
  return pGlobalApp->InitScreen(w,h,screenDepth,screenFlags);
}

void GameApplication::setCaption(const char *title) {
//  pGlobalApp->SetCaption(title,NULL);
}

void GameApplication::showSplash() {
  splash = new PG_ThemeWidget(NULL, PG_Rect(100,100,600,400));
  char file[]="data/graphics/splash_screen.jpg";
  splash->SetBackground(file, PG_Draw::STRETCH);
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
