/*
 * $Id$
 */

#ifndef __BASEAPPLICATION_H__
#define __BASEAPPLICATION_H__

#define WRAPPERTYPE_BASE 1

#include "WorldMap.h"
#include "Engine.h"
#include "Client.h"

class GameMainWindow;

/** Base class for application creation
  * For internal use only!
  */
class BaseApplication {

public:
    /** Should construct application
      * @ref argc and @ref argv are same as given to main() function
      */
    BaseApplication(int argc, char *argv[]);
    /** Should destroy application */
    virtual ~BaseApplication();
    /** Should create and show splash screen */
    virtual void showSplash() {};
    /** Should hide (destroy) splash screen */
    virtual void hideSplash() {};
    /** Should run application. Return value will be used as exit value */
    virtual int run() { return 0; };
    /** Should return type of wrapper as int */
    // Probably this must be made pure too
    virtual int wrapperType() { return WRAPPERTYPE_BASE; };
    /** Should make @ref mw 'main widget' - when user closes it, app
      * will exit
      */
    virtual void setMainWindow(GameMainWindow* mw) {};
    
    virtual void initSingleGame(const std::string, int, int, int) {};
    virtual void initClientGame() {};
    virtual void initServerGame() {};
    
protected:
    GameMainWindow* mW;
    WorldMap* worldMap;
    Engine* engine;
    Player* playerSelf;
    Client* client;
//    Server* server;
};

#endif
