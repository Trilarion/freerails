/*
 * $Id$
 */

#ifndef __GAME_H__
#define __GAME_H__

/*----------------------------------------------------------------------*
 *	General								*
 *----------------------------------------------------------------------*/

#ifndef GAME_VERSION
#define GAME_VERSION	"0.0.7"
#endif

#include "GameApplication.h"
#include "GameWidget.h"

class MyGameApplication : public GameApplication {

public:
    /**  */
    MyGameApplication(int argc, char *argv[]);

    /**  */
    ~MyGameApplication();
    
    void initGame();
    void askUser();

};

#endif