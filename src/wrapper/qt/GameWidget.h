/*
 * $Id$
 */

#ifndef __GAMEWIDGET_H__
#define __GAMEWIDGET_H__

#include "GameMainWindow.h"
#include "BaseWidget.h"

class QResizeEvent;
class QWidget;

class GameWidget : public BaseWidget
{
  public:
    /**  */
    GameWidget(int x, int y, int w, int h, GameMainWindow* parent, const char *name = 0);
    /**  */
    ~GameWidget();

    void show();
    void hide();
    void resizeEvent(QResizeEvent *e);
    QWidget *getWidget() {return widget;}

  private:
    QWidget *widget;
};

#endif
