/*
 * $Id$
 */

#ifndef __TERRAINBUILDPANE_H__
#define __TERRAINBUILDPANE_H__

#include "GameMainWindow.h"

#include <pgthemewidget.h>
#include <pgimage.h>
#include <pglabel.h>

#include "GuiEngine.h"
#include "GameMapView.h"

class TerrainBuildPane: public PG_ThemeWidget {

  public:
    /**  */
    TerrainBuildPane(PG_Widget* parent, int _x, int _y, int _w, int _h,
                    GuiEngine* _engine, GameMapView* _mapView);
    /**  */
    ~TerrainBuildPane();

  protected:
    bool handleOptionButtonClick(PG_Button* button);

  private:

    void releaseAllOptionButtons(PG_Button* button);
    void hideTrackAndBridgeButtons();
    void hideTunnelButtons();
    void hideStationButtons();

    void showTrackAndBridgeButtons();
    void showTunnelButtons();
    void showStationButtons();

    GuiEngine* guiEngine;
    GameMapView* mapView;

    PG_Button* buildButton;
    PG_Button* upgradeButton;
    PG_Button* buildStationButton;
    PG_Button* removeButton;
    PG_Button* buildTrainButton;

    PG_Button* singleTrackButton;
    PG_Button* doubleTrackButton;

    PG_Button* woodenBridgeButton;
    PG_Button* steelBridgeButton;
    PG_Button* stoneBridgeButton;
    PG_Button* noBridgeButton;

    PG_Button* tunnelButton;
    PG_Button* noTunnelButton;

    PG_Button* depotButton;
    PG_Button* stationButton;
    PG_Button* terminalButton;
};

#endif // __TERRAINBUILDPANE_H__
