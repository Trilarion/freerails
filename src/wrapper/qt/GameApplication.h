/*
 * $Id$
 */

#ifndef __GAMEAPPLICATION_H__
#define __GAMEAPPLICATION_H__
#include "BaseApplication.h"

#include "qwidget.h"
#include "qapplication.h"
#include "stdlib.h"
#include "stdio.h"

class GameApplication : public BaseApplication {

public:
    /**  */
    GameApplication(int argc, char *argv[]);
    /**  */
    ~GameApplication();

    bool initScreen(int x, int y, int w, int h);
    void setCaption(const char *title);
    void run();

    /* ONLY for internal use */
    QWidget* getWidget() {return widget;};

private:
    QApplication* application;
    QWidget* widget;

};

#endif