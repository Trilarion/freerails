/*
 * $Id$
 * Mainwindow handling class
 */

#ifndef __GAMEMAINWINDOW_H__
#define __GAMEMAINWINDOW_H__

#include <qwidget.h>

#include "BaseMainWindow.h"

/* GameMainWindow class manages main window
 */
class GameMainWindow : public BaseMainWindow
{
  public:
    /** Creates new MainWindow to position @ref x, @ref y
      * Width and height are set by @ref w and @ref h
      */
    GameMainWindow(int x, int y, int w, int h);
    /** Destroys MainWindow */
    ~GameMainWindow();
    /** Sets caption of MainWindow to @ref caption */
    void setCaption(const char* caption);
    /** Returns MainWindow's widget
      * For internal use only !
      */
    QWidget* getWidget() { return widget; };
  private:
    QWidget* widget;
};

#endif
