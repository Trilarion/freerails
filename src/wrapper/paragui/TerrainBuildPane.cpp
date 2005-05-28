/*
 * $Id$
 */

#include "TerrainBuildPane.h"

TerrainBuildPane::TerrainBuildPane(PG_Widget* parent, int _x, int _y, int _w, int _h,
                                 GuiEngine* _guiEngine, GameMapView* _mapView):
PG_ThemeWidget(parent, PG_Rect(_x,_y,_w,_h), "Widget") {
//  my_parent = parent;
  guiEngine=_guiEngine;
  mapView=_mapView;

  buildButton=new PG_Button(this, PG_Rect(3,5,38,38));
  buildButton->SetIcon("graphics/icons/build_track.png");
  buildButton->SetToggle(true);
  buildButton->sigClick.connect(slot(*this, &TerrainBuildPane::handleOptionButtonClick));

  upgradeButton=new PG_Button(this, PG_Rect(43,5,38,38));
  upgradeButton->SetIcon("graphics/icons/upgrade_track.png");
  upgradeButton->SetToggle(true);
  upgradeButton->sigClick.connect(slot(*this, &TerrainBuildPane::handleOptionButtonClick));

  stationButton=new PG_Button(this, PG_Rect(83,5,38,38));
  stationButton->SetIcon("graphics/icons/build_stations.png");
  stationButton->SetToggle(true);
  stationButton->sigClick.connect(slot(*this, &TerrainBuildPane::handleOptionButtonClick));

  removeButton=new PG_Button(this, PG_Rect(123,5,38,38));
  removeButton->SetIcon("graphics/icons/bulldozer.png");
  removeButton->SetToggle(true);
  removeButton->sigClick.connect(slot(*this, &TerrainBuildPane::handleOptionButtonClick));

  singleTrackButton=new PG_Button(this, PG_Rect(3,45,38,38));
  singleTrackButton->SetIcon("graphics/icons/standard_track.png");
  singleTrackButton->SetToggle(true);

  doubleTrackButton=new PG_Button(this, PG_Rect(43,45,38,38));
  doubleTrackButton->SetIcon("graphics/icons/double_track.png");
  doubleTrackButton->SetToggle(true);

  woodenBridgeButton=new PG_Button(this, PG_Rect(3,85,38,38));
  woodenBridgeButton->SetIcon("graphics/icons/wooden_trestle_bridge.png");
  woodenBridgeButton->SetToggle(true);

  steelBridgeButton=new PG_Button(this, PG_Rect(43,85,38,38));
  steelBridgeButton->SetIcon("graphics/icons/steel_girder_bridge.png");
  steelBridgeButton->SetToggle(true);

  stoneBridgeButton=new PG_Button(this, PG_Rect(83,85,38,38));
  stoneBridgeButton->SetIcon("graphics/icons/stone_masonry_bridge.png");
  stoneBridgeButton->SetToggle(true);

  noBridgeButton=new PG_Button(this, PG_Rect(123,85,38,38));
  noBridgeButton->SetIcon("graphics/icons/no_bridges.png");
  noBridgeButton->SetToggle(true);

  tunnelButton=new PG_Button(this, PG_Rect(3,125,38,38));
  tunnelButton->SetIcon("graphics/icons/tunnel.png");
  tunnelButton->SetToggle(true);

  noTunnelButton=new PG_Button(this, PG_Rect(43,125,38,38));
  noTunnelButton->SetIcon("graphics/icons/no_tunnels.png");
  noTunnelButton->SetToggle(true);
  
  // Startconfig
  buildButton->SetPressed(true);
  singleTrackButton->SetPressed(true);
  noBridgeButton->SetPressed(true);
  noTunnelButton->SetPressed(true);
}

TerrainBuildPane::~TerrainBuildPane() {
}

void TerrainBuildPane::releaseAllOptionButtons(PG_Button* button) {
  if (button!=buildButton) buildButton->SetPressed(false);
  if (button!=upgradeButton) upgradeButton->SetPressed(false);
  if (button!=stationButton) stationButton->SetPressed(false);
  if (button!=removeButton) removeButton->SetPressed(false);
}

bool TerrainBuildPane::handleOptionButtonClick(PG_Button* button) {

  releaseAllOptionButtons(button);
  if (!button->GetPressed()) return true;
  if (button==buildButton)
  {
  } else if (button==upgradeButton)
  {
  } else if (button==stationButton)
  {
  } else if (button==removeButton)
  {
  }
  return true;
}
