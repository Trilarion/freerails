/*
 * $Id$
 */

#ifndef __GAMEWIDGET_H__
#define __GAMEWIDGET_H__

#include <qwidget.h>

#include "GameApplication.h"

class GameWidget {

  public:
    /**  */
    GameWidget();
    GameWidget(GameWidget* parent, int x, int y, int w, int h);
    GameWidget(GameApplication* parent, int x, int y, int w, int h);
    /**  */
    ~GameWidget();

    void show();
    void hide();
    
    /* ONLY for internal use */
    QWidget* getWidget() {return widget;};

  protected:
    QWidget* widget;

};

#endif