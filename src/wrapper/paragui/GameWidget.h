/*
 * $Id$
 */

#ifndef __GAMEWIDGET_H__
#define __GAMEWIDGET_H__

#include <pgwidget.h>
#include <pgrect.h>

#include "GameApplication.h"

class GameWidget {

  public:
    /**  */
    GameWidget(GameWidget* parent, int x, int y, int w, int h);
    GameWidget(GameApplication* parent, int x, int y, int w, int h);
    /**  */
    ~GameWidget();

    void show();
    void hide();
    
    /* ONLY for internal use */
    PG_Widget* getWidget() {return widget;};

  protected:
    PG_Widget* widget;

};

#endif