/*
 * $Id$
 */

#ifndef __GAMEDATASELECTDIALOG_H__
#define __GAMEDATASELECTDIALOG_H__

#include "GameMainWindow.h"

#include <pgwidget.h>
#include <pgwindow.h>
#include <pgrect.h>
#include <pgrectlist.h>
#include <pgdropdown.h>
#include <pgradiobutton.h>
#include <pgspinnerbox.h>
#include <pgmaskedit.h>

class GameDataSelectDialog: public PG_Window {

  public:
    /**  */
    GameDataSelectDialog(GameMainWindow* parent, int x, int y, int w, int h, char* title, int type);
    /**  */
    ~GameDataSelectDialog();

    int show();
    
    std::string getName() {return std::string(name->GetText());};
    int getPort() {return atoi(port->GetText().c_str());};
    char *getIpAddress() {return strtok((char *)ip_address->GetText().c_str(),"_");};
    
    int getWidth() {return width->GetValue();};
    int getHeight() {return height->GetValue();};

  protected:
    bool handleButtonClick(PG_Button* button);
    bool handleRadioClick(PG_RadioButton* button);

  private:
//    bool eventButtonClick(int id, PG_Widget* widget);
    int buttonflag;
    int type;
    PG_DropDown* name;
    PG_SpinnerBox* width;
    PG_SpinnerBox* height;
    PG_MaskEdit* ip_address;
    PG_MaskEdit* port;
    PG_Button* ok;
    PG_Button* back;
    PG_RadioButton* radio_server;
    PG_RadioButton* radio_client;
};

#endif
