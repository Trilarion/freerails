/*
 * $Id$
 */

#ifndef __GAMEPANEL_H__
#define __GAMEPANEL_H__

#include "GameMainWindow.h"

#include <pggradientwidget.h>
#include <pgrect.h>

class GamePanel: public PG_GradientWidget {

  public:
    /**  */
    GamePanel(GameMainWindow* parent, int x, int y, int w, int h);
    /**  */
    ~GamePanel();

  private:

};

#endif