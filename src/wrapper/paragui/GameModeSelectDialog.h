/*
 * $Id$
 */

#ifndef __GAMEMODESELECTDIALOG_H__
#define __GAMEMODESELECTDIALOG_H__

#include "GameMainWindow.h"

#include <pgwidget.h>
#include <pgwindow.h>
#include <pgrect.h>
#include <pgrectlist.h>

class GameModeSelectDialog: public PG_Window {

  public:
    /**  */
    GameModeSelectDialog(GameMainWindow* parent, int x, int y, int w, int h, char* title);
    /**  */
    ~GameModeSelectDialog();

    int show();
    bool eventButtonClick(int id, PG_Widget* widget);

  private:

    int buttonflag;
    int WaitForEvent();
    PG_Button* single;
    PG_Button* multi;
    PG_Button* quit;
};

#endif
