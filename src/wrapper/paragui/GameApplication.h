/*
 * $Id$
 */

#ifndef __GAMEAPPLICATION_H__
#define __GAMEAPPLICATION_H__
#include "BaseApplication.h"
#include "GameMainWindow.h"
#include "SDL.h"


#include <pgapplication.h>
#include <pggradientwidget.h>

#define WRAPPERTYPE_PARAGUI 2 

class GameApplication : public BaseApplication {

public:
    GameApplication(int argc, char *argv[]);
    /**  */
    ~GameApplication();

    bool initScreen(int x, int y, int w, int h);
    void setCaption(const char *title);
    int run();
    int wrapperType() { return WRAPPERTYPE_PARAGUI; };
    void showSplash();
    void hideSplash();
    void setMainWindow(GameMainWindow* mw);

private:
    struct PG_Application* pGlobalApp;
    Uint32 screenFlags;
    int screenDepth;
    PG_GradientWidget* splash;
};

#endif