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

    bool InitScreen(int x, int y, int w, int h);
    void SetCaption(const char *title);
    void Run();

private:
    struct PG_Application* pGlobalApp;
    Uint32 screenFlags;
    int screenDepth;

};

#endif