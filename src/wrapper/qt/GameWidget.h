/*
 * $Id$
 */

#ifndef __GAMEWIDGET_H__
#define __GAMEWIDGET_H__

#include "qwidget.h"

#include "GameMainWindow.h"
#include "BaseWidget.h"

class QResizeEvent;
class QWidget;

class GameWidget : public QWidget, BaseWidget
{
  public:
    /**  */
    GameWidget(GameMainWindow* _parent = NULL, const char *name = NULL);
    /**  */
    ~GameWidget();

    void show();
    void hide();
/*
    void resizeEvent(QResizeEvent *e);
*/    void setGeometry(int x, int y, int w, int h);
    void setCaption(const char *name);

    int width() { return QWidget::width(); };
    int height() { return QWidget::height(); };

  private:
    GameMainWindow* parent;

};

#endif
