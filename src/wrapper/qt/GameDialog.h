/*
 * $Id$
 */

#ifndef __GAMEDIALOG_H__
#define __GAMEDIALOG_H__

#include "BaseDialog.h"

#include "GameApplication.h"
#include "GameWidget.h"

#include <qdialog.h>

class GameDialog: public GameWidget
{
//    Q_OBJECT
    
  public:
    /**  */
    GameDialog(GameMainWindow* parent, int x, int y, int w, int h, char* title);
    /**  */
    ~GameDialog();

    void show();

  private:
    QDialog *dlg;
    
};

#endif
