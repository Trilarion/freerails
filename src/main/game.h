/*
 * $Id$
 */

#ifndef __GAME_H__
#define __GAME_H__

/*----------------------------------------------------------------------*
 *	General								*
 *----------------------------------------------------------------------*/

/* It's #define in config.h :) */
/*
#ifndef GAME_VERSION
#define GAME_VERSION	"0.0.7+"
#endif
*/

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

    // definition of a single game    
    // for future use:
    //     numberOfAi: select the number of your Ai's
    //     playFieldWidth = -1 mean we will play a scenario,
    //     where playFieldHeight is the number of scenario
    void initSingleGame(const std::string name, int playFieldWidth, int playFieldHeight, int numberOfAi);
    void initClientGame(const std::string name);
    void initServerGame(const std::string name, int playFieldWidth, int playFieldHeight, int numberOfAi);

};

#endif
