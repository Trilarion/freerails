/*
 * $Id$
 */

#ifndef __GAMEPANEL_H__
#define __GAMEPANEL_H__

#include "GameMainWindow.h"

#include <pggradientwidget.h>
#include <pgbutton.h>
#include <pgrect.h>

#include <pglineedit.h>
#include <pgrichedit.h>

#include <pgeventobject.h>

#include "Engine.h"
#include "Message.h"

class GamePanel: public PG_GradientWidget, public PG_EventObject {

  public:
    /**  */
    GamePanel(GameMainWindow* parent, int x, int y, int w, int h, Engine* _engine);
    /**  */
    ~GamePanel();

  private:
  
    PARAGUI_CALLBACK(pause_handler);

    PG_Button* trackButton;
    PG_Button* stationButton;
    PG_Button* pauseButton;
    Engine* engine;
};

#endif