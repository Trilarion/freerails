/*
 * $Id$
 */

#ifndef __GAMENETVIEW_H__
#define __GAMENETVIEW_H__

#include "GameMainWindow.h"

#include <pgthemewidget.h>
#include <pgrect.h>

#include <pglineedit.h>
#include <pgrichedit.h>

class GameNetView: public PG_ThemeWidget {

  public:
    /**  */
    GameNetView(GameMainWindow* parent, int x, int y, int w, int h);
    /**  */
    ~GameNetView();

  private:

    PG_LineEdit* lineedit;
    PG_RichEdit* richedit;
};

#endif