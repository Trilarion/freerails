/*
 * $Id$
 */

#include "GameModeSelectDialog.h"

GameModeSelectDialog::GameModeSelectDialog(GameMainWindow* parent, int x, int y, int w, int h, char* titel):
PG_Window(parent->getWidget(), PG_Rect(x,y,w,h), titel, PG_Window::MODAL) {
  single=new PG_Button(this, PG_Rect(10,100,280,20), "Single Player Mode", 1);
  multi=new PG_Button(this, PG_Rect(10,130,280,20), "Multi Player Mode", 2);
  quit=new PG_Button(this, PG_Rect(10,170,280,20), "Quit Game", PG_Button::CANCEL);

  single->sigClick.connect(slot(*this, &GameModeSelectDialog::handleButtonClick));
  multi->sigClick.connect(slot(*this, &GameModeSelectDialog::handleButtonClick));
  quit->sigClick.connect(slot(*this, &GameModeSelectDialog::handleButtonClick));
  buttonflag=0;
}

GameModeSelectDialog::~GameModeSelectDialog() {
    delete single;
    delete multi;
    delete quit;
}

int GameModeSelectDialog::show() {

  buttonflag=0;

  Show();
  RunModal();
  Hide();
  return buttonflag;
}

bool GameModeSelectDialog::handleButtonClick(PG_Button* button) {
  //Button clicked?
  
  if(button==single || button==multi || button==quit) {
    //Set Buttonflag to ButtonID
    buttonflag=button->GetID();
    QuitModal();
    return true;
  }
  return false;
}
