/*
 * $Id$
 */

#ifndef __GAMEDIALOG_H__
#define __GAMEDIALOG_H__

#include "BaseDialog.h"

#include "GameApplication.h"
#include "GameWidget.h"

#include <qwidget.h>
#include <qdialog.h>

class GameDialog: public GameWidget {

  public:
    /**  */
    GameDialog(GameWidget* parent, int x, int y, int w, int h, char* titel);
    GameDialog(GameApplication* parent, int x, int y, int w, int h, char* titel);
    /**  */
    ~GameDialog();

    void show();
    
};

#endif