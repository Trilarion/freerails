/*
 * $Id$
 */

#ifndef __GAMEDATASELECTDIALOG_H__
#define __GAMEDATASELECTDIALOG_H__

#include "GameMainWindow.h"

#include <pgwidget.h>
#include <pgwindow.h>
#include <pgrect.h>
#include <pgrectlist.h>
#include <pgdropdown.h>
#include <pgspinnerbox.h>

class GameDataSelectDialog: public PG_Window {

  public:
    /**  */
    GameDataSelectDialog(GameMainWindow* parent, int x, int y, int w, int h, char* title, int type);
    /**  */
    ~GameDataSelectDialog();

    int show();
    
    std::string getName() {return std::string(name->GetText());};
    
    int getWidth() {return width->GetValue();};
    int getHeight() {return height->GetValue();};


  private:

    bool eventButtonClick(int id, PG_Widget* widget);
    int buttonflag;
    PG_DropDown* name;
    PG_SpinnerBox* width;
    PG_SpinnerBox* height;
    PG_Button* ok;
    PG_Button* back;
};

#endif
