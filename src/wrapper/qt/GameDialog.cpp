/*
 * $Id$
 */

#include "GameDialog.h"
#include <qnamespace.h>

GameDialog::GameDialog(GameWidget* parent, int x, int y, int w, int h, char* title)
{
  widget=new QDialog(parent->getWidget(), title,true,0x00000010|0x00000080);
  widget->setGeometry(x,y,w,h);
}

GameDialog::GameDialog(GameMainWindow* parent, int x, int y, int w, int h, char* title)
{
  widget=new QDialog(parent->getWidget(), title, true,
      widget->WStyle_Customize | widget->WStyle_Title |
      widget->WStyle_NormalBorder);
  widget->setGeometry(x,y,w,h);
}

GameDialog::~GameDialog() {

}

void GameDialog::show() {
  ((QDialog*)widget)->exec();
}
