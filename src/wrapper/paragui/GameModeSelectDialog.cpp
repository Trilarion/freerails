/*
 * $Id$
 */

#include "GameModeSelectDialog.h"

GameModeSelectDialog::GameModeSelectDialog(GameMainWindow* parent, int x, int y, int w, int h, char* titel):
PG_Window(parent->getWidget(), PG_Rect(x,y,w,h),titel,0, "Window") {
  single=new PG_Button(this, 1, PG_Rect(10,100,280,20), "Single Player Mode");
  multi=new PG_Button(this, 2, PG_Rect(10,130,280,20), "Multi Player Mode");
  quit=new PG_Button(this, -1, PG_Rect(10,170,280,20), "Quit Game");
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
  int i = WaitForEvent();
  Hide();
  return i;
}

//Event?
bool GameModeSelectDialog::eventButtonClick(int id, PG_Widget* widget) {
	//Button clicked?
	if(widget==single || widget==multi || widget==quit) {
		//Set Buttonflag to ButtonID
		buttonflag=id;
		return true;
	}
	return false;
}

int GameModeSelectDialog::WaitForEvent() {

  int help;
  SDL_Event event;
  
  while(!buttonflag){
    SDL_WaitEvent(&event);
    PG_RectList* childlist = GetChildList();
    if(childlist) {
      PG_RectList::iterator i = childlist->begin();
      while(i != childlist->end()) {
        (*i)->ProcessEvent(&event);
        i++;
      }
    }
    Update();
  }
  
  help = buttonflag;
  buttonflag=0;
  while(SDL_PollEvent(&event));
  return help;
}
