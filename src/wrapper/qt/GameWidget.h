/*
 * $Id$
 */

#ifndef __GAMEWIDGET_H__
#define __GAMEWIDGET_H__

#include <qwidget.h>

#include "GameMainWindow.h"
#include "BaseWidget.h"

class GameWidget : public BaseWidget {

  public:
    /**  */
    GameWidget();
    GameWidget(GameWidget* parent, int x, int y, int w, int h);
    GameWidget(GameMainWindow* parent, int x, int y, int w, int h);
    /**  */
    ~GameWidget();

    void show();
    void hide();
    
    /* ONLY for internal use */
    QWidget* getWidget() { return widget; };

  protected:
    QWidget* widget;

};

#endif
