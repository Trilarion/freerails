/*
 * $Id$
 */

#ifndef __GAMEAPPLICATION_H__
#define __GAMEAPPLICATION_H__
#include "BaseApplication.h"
#include "GameDataSelectDialog.h"
#include "GameMainWindow.h"
#include "GameModeSelectDialog.h"
#include "GameNetView.h"
#include "GamePanel.h"
#include "GameFrameHandler.h"
#include "GameNetHandler.h"

#include "SDL.h"

#include "Message.h"


#include <pgapplication.h>
#include <pgframeapplication.h>
#include <pgthemewidget.h>

#include <unistd.h>

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
    static int runEngine(void* data);

private:
    struct PG_FrameApplication* pGlobalApp;
    Uint32 screenFlags;
    int screenDepth;
    PG_ThemeWidget* splash;
    GameNetView* netView;
    GamePanel* panel;
    PG_SpriteBase terrainbase;
    PG_SpriteBase trackbase;
    MapHelper* mapHelper;
};

#endif
