/*
 * $Id$
 */

#ifndef __GAMEAPPLICATION_H__
#define __GAMEAPPLICATION_H__
#include "BaseApplication.h"
#include "SDL.h"

#include <pgapplication.h>
#include <pgwidgetlist.h>

class GameApplication : public BaseApplication {

public:
    /**  */
    GameApplication(int argc, char *argv[]);
    /**  */
    ~GameApplication();

    bool initScreen(int x, int y, int w, int h);
    void setCaption(const char *title);
    void run();

private:
    struct PG_Application* pGlobalApp;
    Uint32 screenFlags;
    int screenDepth;

};

#endif