/*
 * $Id$
 */

#include "GameDialog.h"

PARAGUI_CALLBACK(handle_dialog_close) {
  GameDialog* dialog = (GameDialog*)clientdata;
  dialog->setButtonflag(-1);
  return true;
}

GameDialog::GameDialog(GameWidget* parent, int x, int y, int w, int h, char* titel) {
  widget=new PG_Window(parent->getWidget(), PG_Rect(x,y,w,h),titel,true);
  buttonflag=0;
  widget->SetEventCallback(MSG_WINDOWCLOSE,handle_dialog_close,this);
}

GameDialog::GameDialog(GameApplication* parent, int x, int y, int w, int h, char* titel) {
  widget=new PG_Window(NULL, PG_Rect(x,y,w,h),titel,true);
  buttonflag=0;
  widget->SetEventCallback(MSG_WINDOWCLOSE,handle_dialog_close,this);
}

GameDialog::~GameDialog() {

}

int GameDialog::show() {

  ((PG_Window*)widget)->Show();
  return WaitForEvent();
}

int GameDialog::WaitForEvent() {

  int help;
  SDL_Event event;
  
  while(!buttonflag){
    SDL_WaitEvent(&event);
    widget->ProcessEvent(&event);
  }
  
  help = buttonflag;
  buttonflag=0;
  while(SDL_PollEvent(&event));
  return help;
}