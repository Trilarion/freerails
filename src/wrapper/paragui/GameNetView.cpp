/*
 * $Id$
 */

#include "GameNetView.h"

GameNetView::GameNetView(GameMainWindow* parent, int x, int y, int w, int h):
PG_ThemeWidget(parent->getWidget(), PG_Rect(x,y,w,h), "ThemeWidget") {
//  SetBackgroundBlend(0);
			 
  richedit=new PG_RichEdit(this, PG_Rect(0,0,w,h-30));
  richedit->SetText("Aloahe");
  lineedit=new PG_LineEdit(this, PG_Rect(0,h-30,w,30));
}

GameNetView::~GameNetView() {
  delete richedit;
  delete lineedit;
}
