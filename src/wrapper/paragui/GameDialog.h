/*
 * $Id$
 */

#ifndef __GAMEDIALOG_H__
#define __GAMEDIALOG_H__

#include "BaseDialog.h"

#include "GameApplication.h"
#include "GameWidget.h"

#include <pgwidget.h>
#include <pgwindow.h>
#include <pgrect.h>

class GameDialog: public GameWidget {

  public:
    /**  */
    GameDialog(GameWidget* parent, int x, int y, int w, int h, char* titel);
    GameDialog(GameApplication* parent, int x, int y, int w, int h, char* titel);
    /**  */
    ~GameDialog();

    int show();
    
    void setButtonflag(int i) {buttonflag=i;};

    PARAGUI_CALLBACK(handle_dialog_exit) {
      PG_Button* but = (PG_Button*)clientdata;
      buttonflag = id;
      return true;
    }

  private:
    int WaitForEvent();
    int buttonflag;

};

#endif