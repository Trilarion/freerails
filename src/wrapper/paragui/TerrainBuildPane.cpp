/*
 * $Id$
 */

#include "TerrainBuildPane.h"
#include "Station.h"

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

  buildStationButton=new PG_Button(this, PG_Rect(83,5,38,38));
  buildStationButton->SetIcon("graphics/icons/build_stations.png");
  buildStationButton->SetToggle(true);
  buildStationButton->sigClick.connect(slot(*this, &TerrainBuildPane::handleOptionButtonClick));

  removeButton=new PG_Button(this, PG_Rect(123,5,38,38));
  removeButton->SetIcon("graphics/icons/bulldozer.png");
  removeButton->SetToggle(true);
  removeButton->sigClick.connect(slot(*this, &TerrainBuildPane::handleOptionButtonClick));

  buildTrainButton=new PG_Button(this, PG_Rect(3,165,38,38));
  buildTrainButton->SetIcon("graphics/ui/buttons/build_train.png");
  buildTrainButton->SetToggle(true);
  buildTrainButton->sigClick.connect(slot(*this, &TerrainBuildPane::handleOptionButtonClick));

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

  depotButton=new PG_Button(this, PG_Rect(43,45,38,38));
  depotButton->SetIcon("graphics/icons/depot.png");
  depotButton->SetToggle(true);

  stationButton=new PG_Button(this, PG_Rect(83,45,38,38));
  stationButton->SetIcon("graphics/icons/station.png");
  stationButton->SetToggle(true);

  terminalButton=new PG_Button(this, PG_Rect(123,45,38,38));
  terminalButton->SetIcon("graphics/icons/terminal.png");
  terminalButton->SetToggle(true);
  
  // Startconfig
  hideTrackAndBridgeButtons();
  hideTunnelButtons();
  hideStationButtons();

  singleTrackButton->SetPressed(true);
  noBridgeButton->SetPressed(true);
  noTunnelButton->SetPressed(true);
  depotButton->SetPressed(true);

  mapView->setMouseType(GameMapView::normal);
  mapView->setStationType(Station::Small);
}

TerrainBuildPane::~TerrainBuildPane() {
}

void TerrainBuildPane::releaseAllOptionButtons(PG_Button* button) {
  if (button!=buildButton) buildButton->SetPressed(false);
  if (button!=upgradeButton) upgradeButton->SetPressed(false);
  if (button!=buildStationButton) buildStationButton->SetPressed(false);
  if (button!=removeButton) removeButton->SetPressed(false);
  if (button!=buildTrainButton) buildTrainButton->SetPressed(false);
}

void TerrainBuildPane::hideTrackAndBridgeButtons() {
  singleTrackButton->Hide();
  doubleTrackButton->Hide();
  woodenBridgeButton->Hide();
  steelBridgeButton->Hide();
  stoneBridgeButton->Hide();
  noBridgeButton->Hide();
}

void TerrainBuildPane::showTrackAndBridgeButtons() {
  singleTrackButton->Show();
  doubleTrackButton->Show();
  woodenBridgeButton->Show();
  steelBridgeButton->Show();
  stoneBridgeButton->Show();
  noBridgeButton->Show();
}

void TerrainBuildPane::hideTunnelButtons() {
  tunnelButton->Hide();
  noTunnelButton->Hide();
}

void TerrainBuildPane::showTunnelButtons() {
  tunnelButton->Show();
  noTunnelButton->Show();
}

void TerrainBuildPane::hideStationButtons() {
  depotButton->Hide();
  stationButton->Hide();
  terminalButton->Hide();
}

void TerrainBuildPane::showStationButtons() {
  depotButton->Show();
  stationButton->Show();
  terminalButton->Show();
}

bool TerrainBuildPane::handleOptionButtonClick(PG_Button* button) {

  releaseAllOptionButtons(button);
  if (!button->GetPressed())
  {
    hideTrackAndBridgeButtons();
    hideTunnelButtons();
    hideStationButtons();
    mapView->setMouseType(GameMapView::normal);
    return true;
  }
  if (button==buildButton)
  {
    mapView->setMouseType(GameMapView::buildTrack);
    hideStationButtons();
    showTrackAndBridgeButtons();
    showTunnelButtons();
  } else if (button==upgradeButton)
  {
    mapView->setMouseType(GameMapView::updateTrack);
    hideTunnelButtons();
    hideStationButtons();
    showTrackAndBridgeButtons();
  } else if (button==buildStationButton)
  {
    mapView->setMouseType(GameMapView::buildStation);
    hideTrackAndBridgeButtons();
    hideTunnelButtons();
    showStationButtons();
  } else if (button==removeButton)
  {
    mapView->setMouseType(GameMapView::removeTrack);
    hideTrackAndBridgeButtons();
    hideTunnelButtons();
    hideStationButtons();
  } else if (button==buildTrainButton)
  {
    mapView->setMouseType(GameMapView::buildTrain);
    hideTrackAndBridgeButtons();
    hideTunnelButtons();
    hideStationButtons();
  }
  return true;
}
