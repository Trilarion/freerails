/*
 * $Id$
 */

#include "SelectTrainTypeDialog.h"

SelectTrainTypeDialog::SelectTrainTypeDialog(PG_Widget* parent, int x, int y, int w, int h, GuiEngine* _engine):
PG_Window(parent, PG_Rect(x,y,w,h),"", PG_Window::MODAL) {

  Hide();
  SetTransparency(90);
  engine = _engine;
  ok=new PG_Button(this, PG_Rect(10,h-30,130,20), "BUild Train", PG_Button::OK);
  back=new PG_Button(this, PG_Rect(160,h-30,130,20), "Cancel", PG_Button::CANCEL);
  ok->sigClick.connect(slot(*this, &SelectTrainTypeDialog::handleButtonClick));
  back->sigClick.connect(slot(*this, &SelectTrainTypeDialog::handleButtonClick));
}

SelectTrainTypeDialog::~SelectTrainTypeDialog() {
}

int SelectTrainTypeDialog::show() {

  Show();
}

bool SelectTrainTypeDialog::handleButtonClick(PG_Button* button) {

  //Set Buttonflag to the ButtonID of pressed button
  Hide();
  return true;
}
