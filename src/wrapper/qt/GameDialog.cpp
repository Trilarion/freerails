/*
 * $Id$
 */

#include "GameDialog.h"

GameDialog::GameDialog(GameMainWindow* parent, int x, int y, int w, int h, char* title) :
 GameWidget(parent, title)
{
  dlg = new QDialog(0, title, true,
    Qt::WStyle_Customize | Qt::WStyle_Title | Qt::WStyle_NormalBorder);
  dlg->setGeometry(x,y,w,h);
}

GameDialog::~GameDialog()
{

}

void GameDialog::show()
{
  dlg->exec();
}
