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
    ~BaseApplication();
    /** Should create and show splash screen */
    virtual void showSplash() = 0;
    /** Should hide (destroy) splash screen */
    virtual void hideSplash() = 0;
    /** Should run application. Return value will be used as exit value */
    virtual int run() = 0;
    /** Should return type of wrapper as int */
    // Probably this must be made pure too
    virtual int wrapperType() { return WRAPPERTYPE_BASE; };
    /** Should make @ref mw 'main widget' - when user closes it, app
      * will exit
      */
    virtual void setMainWindow(GameMainWindow* mw) = 0;
    
    virtual void initSingleGame() {};
    virtual void initClientGame() {};
    virtual void initServerGame() {};
    
protected:
    WorldMap* worldMap;
    Engine* engine;
    Player* playerSelf;
    Client* client;
//  Server* server;
};

#endif
