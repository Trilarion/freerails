/*
 * $Id$
 */

#ifndef __GAME_H__
#define __GAME_H__

/*----------------------------------------------------------------------*
 *	General								*
 *----------------------------------------------------------------------*/

#ifndef GAME_VERSION
#define GAME_VERSION	"0.0.7+"
#endif

#include "GameApplication.h"
#include "GameWidget.h"
#include "GameDialog.h"

#include <string>

class MyGameApplication : public GameApplication {

public:
    /**  */
    MyGameApplication(int argc, char *argv[]);

    /**  */
    ~MyGameApplication();
    
    void initSingleGame();
    void initClientGame();
    void initServerGame();

};

#endif