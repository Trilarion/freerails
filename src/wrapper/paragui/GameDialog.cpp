/*
 * $Id$
 */

#include "GameDialog.h"
#include <pgmessagebox.h>
GameDialog::GameDialog(GameWidget* parent, int x, int y, int w, int h, char* titel) {
  widget=new PG_Window(parent->getWidget(), PG_Rect(x,y,w,h),titel,true);
}

GameDialog::GameDialog(GameApplication* parent, int x, int y, int w, int h, char* titel) {
  widget=new PG_Window(NULL, PG_Rect(x,y,w,h),titel,true);  
}

GameDialog::~GameDialog() {

}

int GameDialog::show() {

  ((PG_Window*)widget)->Show();
  return WaitForEvent();
}

int GameDialog::WaitForEvent() {

  //TODO Build loop
  return 0;
}