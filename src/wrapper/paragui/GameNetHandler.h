/*
 * $Id$
 */

#ifndef __GAMENETHANDLER_H__
#define __GAMENETHANDLER_H__

#include <pgtimerobject.h>
#include <pgnethandler.h>

#include "GameMainWindow.h"
#include "GamePanel.h"
#include "GameMapView.h"
#include "GameElement.h"
#include "GameFrameHandler.h"
#include "GuiEngine.h"
#include "Message.h"
#include "Station.h"

class GameNetHandler : public PG_NetHandler {

  public:
    /**  */
    GameNetHandler(GameMainWindow* _mw, GuiEngine* _guiEngine, GamePanel* _panel, GameMapView *_mapView);
    /**  */
    virtual ~GameNetHandler();

    void checkNet();

  protected:
    void addings(GameElement* element);
    
  private:
    GuiEngine* guiEngine;
    GamePanel* panel;
    GameMainWindow* mw;
    GameMapView *mapView;
    GameFrameHandler *framehandler;
};

#endif
