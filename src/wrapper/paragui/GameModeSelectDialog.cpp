/*
 * $Id$
 */

#include "GameModeSelectDialog.h"

PARAGUI_CALLBACK(handle_dialog_close) {
  GameModeSelectDialog* dialog = (GameModeSelectDialog*)clientdata;
  dialog->setButtonflag(-1);
  return true;
}

GameModeSelectDialog::GameModeSelectDialog(GameMainWindow* parent, int x, int y, int w, int h, char* titel) {
  widget=new PG_Window(parent->getWidget(), PG_Rect(x,y,w,h),titel,true);
  widget->SetEventCallback(MSG_WINDOWCLOSE,handle_dialog_close,this);
}

GameModeSelectDialog::~GameModeSelectDialog() {
  if (widget)
    delete widget;
}

int GameModeSelectDialog::show() {

  buttonflag=0;

  PG_Button single(widget, 1, PG_Rect(10,130,280,40), "Single Player Mode");
  single.SetEventCallback(MSG_BUTTONCLICK, handle_dialog_close, this);
  single.Show();

  PG_Button multi(widget, 2, PG_Rect(10,180,280,40), "Multi Player Mode");
  multi.SetEventCallback(MSG_BUTTONCLICK, handle_dialog_close, this);
  multi.Show();

  PG_Button quit(widget, -1, PG_Rect(10,250,280,40), "Quit Game");
  quit.SetEventCallback(MSG_BUTTONCLICK, handle_dialog_close, this);
  quit.Show();

  widget->Show();
  int i = WaitForEvent();
  widget->Hide();
  return i;
}

int GameModeSelectDialog::WaitForEvent() {

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