/*
 * $Id$
 */

#include "GameDataSelectDialog.h"

GameDataSelectDialog::GameDataSelectDialog(GameMainWindow* parent, int x, int y, int w, int h, char* titel, int _type):
PG_Window(parent->getWidget(), PG_Rect(x,y,w,h),titel,0, "Window") {

  type = _type;
  name=new PG_DropDown(this, 10, PG_Rect(10,30,240,20));
  name->SetIndent(5);
  name->SetText("Your Name");
  name->AddItem("Thomas Cook");
  name->AddItem("Erich Frenzel");
  name->AddItem("Blubber Bla Bla");
  name->AddItem("Dr. Ing. Blubber");

  width=new PG_SpinnerBox(this, PG_Rect(260,30,150,20));
  width->SetMask("####");
  width->SetValue(30);
  width->SetMinValue(30);
  width->SetMaxValue(1000);

  height=new PG_SpinnerBox(this, PG_Rect(260,60,150,20));
  height->SetMask("####");
  height->SetValue(30);
  height->SetMinValue(30);
  height->SetMaxValue(1000);

  ok=new PG_Button(this, type, PG_Rect(10,260,180,20), "OK");
  back=new PG_Button(this, -1, PG_Rect(210,260,180,20), "Zurück");
  buttonflag=0;
  
  if (type>1)
  {
    radio_server = new PG_RadioButton(this, 20, PG_Rect(10, 60, 240, 20), "Be a server");
    radio_client = new PG_RadioButton(this, 21, PG_Rect(10, 90, 240, 20), "Be a client", radio_server);
    ip_adress = new PG_MaskEdit(this, PG_Rect(10,120,240,20));
    ip_adress->SetMask("###.###.###.###");
    port = new PG_MaskEdit(this, PG_Rect(260,120,100,20));
    port->SetMask("#####");
    port->SetText("9010");
  }
}

GameDataSelectDialog::~GameDataSelectDialog() {
  if (type>1)
  {
    delete radio_client;
    delete radio_server;
    delete ip_adress;
  }
  delete name;
  delete width;
  delete height;
  delete ok;
  delete back;
}

int GameDataSelectDialog::show() {

  buttonflag=0;

  Show();
  RunModal();
  Hide();
  return buttonflag;
}

//Event?
bool GameDataSelectDialog::eventButtonClick(int id, PG_Widget* widget) {
  //Button clicked?
  if(widget==back) {
    //Set Buttonflag to ButtonID
    buttonflag=id;
    SendMessage(this, MSG_MODALQUIT, 0, 0);
    return true;
  } else
  if (widget==ok)
  {
    std::cerr << getName() << std::endl;
    //TODO taste if name != "" ist
    buttonflag=id;
    SendMessage(this, MSG_MODALQUIT, 0, 0);
    return true;
  } else
  if (widget==radio_server)
  {
    if (radio_server->GetPressed())
    {
      width->Show();
      height->Show();
      ip_adress->Hide();
      port->Hide();
    }
    return true;
  } else
  if (widget==radio_client)
  {
    if (radio_client->GetPressed())
    {
      width->Hide();
      height->Hide();
      ip_adress->Show();
      port->Show();
    }
    return true;
  }
  return false;
}
