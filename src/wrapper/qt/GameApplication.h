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

    bool InitScreen(int x, int y, int w, int h);
    void SetCaption(const char *title);
    void Run();
    
private:
    QApplication* application;
    QWidget* widget;

};

#endif