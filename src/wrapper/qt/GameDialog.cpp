/*
 * $Id$
 */

#include "GameDialog.h"
#include <qnamespace.h>

GameDialog::GameDialog(GameWidget* parent, int x, int y, int w, int h, char* titel) {
  widget=new QDialog(parent->getWidget(), titel,true,0x00000010|0x00000080);
  widget->setGeometry(x,y,w,h);
}

GameDialog::GameDialog(GameApplication* parent, int x, int y, int w, int h, char* titel) {
  widget=new QDialog(parent->getWidget(), titel,true,widget->WStyle_Customize|widget->WStyle_Title|widget->WStyle_NormalBorder);
  widget->setGeometry(x,y,w,h);
}

GameDialog::~GameDialog() {

}

void GameDialog::show() {
  ((QDialog*)widget)->exec();
}
