/*
 * $Id$
 */

#ifndef __GAMEMODESELECTDIALOG_H__
#define __GAMEMODESELECTDIALOG_H__

#include "GameApplication.h"

#include "BaseDialog.h"

#include <pgwidget.h>
#include <pgwindow.h>
#include <pgrect.h>

class GameModeSelectDialog: public BaseDialog {

  public:
    /**  */
    GameModeSelectDialog(GameMainWindow* parent, int x, int y, int w, int h, char* title);
    /**  */
    ~GameModeSelectDialog();

    int show();
    void setButtonflag(int i) { buttonflag=i;};    

    PARAGUI_CALLBACK(handle_dialog_exit) {
      PG_Button* but = (PG_Button*)clientdata;
      buttonflag = id;
      return true;
    }

  private:

    PG_Widget* widget;
    int buttonflag;
    int WaitForEvent();
};

#endif