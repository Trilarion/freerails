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

#ifdef USE_PARAGUI
#include "WrapperParaGUI.h"
class Game_Application : public WrapperParaGUI {
#endif

#ifdef USE_QT
#include "WrapperQt.h"
class Game_Application : public WrapperQt {
#endif


public:
    /**  */
    Game_Application(int argc, char *argv[]);

    /**  */
    ~Game_Application();
    
    void InitGame();
    void AskUser();

};

#endif