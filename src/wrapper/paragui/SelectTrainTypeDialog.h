/*
 * $Id$
 */

#ifndef __SELECTTRAINTYPEDIALOG_H__
#define __SELECTTRAINTYPEDIALOG_H__

#include "GuiEngine.h"

#include <pgwidget.h>
#include <pgbutton.h>
#include <pgwindow.h>
#include <pgthemewidget.h>
#include <pgrect.h>
#include <pgrectlist.h>
#include <pgdropdown.h>
#include <pgradiobutton.h>
#include <pgspinnerbox.h>
#include <pgmaskedit.h>

class SelectTrainTypeDialog: public PG_Window {

  public:
    /**  */
    SelectTrainTypeDialog(PG_Widget* parent, int x, int y, int w, int h, GuiEngine* _engine);
    /**  */
    ~SelectTrainTypeDialog();

    int show();
    
    int getTrainType() {return 0;};

    bool eventMouseMotion(const SDL_MouseMotionEvent* motion) { return true; };
    bool eventMouseButtonDown(const SDL_MouseButtonEvent* button) { return true; };
    bool eventMouseButtonUp(const SDL_MouseButtonEvent* button) { return true; };

  protected:
    bool handleButtonClick(PG_Button* button);
    bool handleRadioClick(PG_RadioButton* button);

  private:
    GuiEngine* engine;
    PG_Button* ok;
    PG_Button* back;
};

#endif
