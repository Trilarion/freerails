/*
 * $Id$
 */

#include "TerrainBuildPane.h"

TerrainBuildPane::TerrainBuildPane(PG_Widget* parent, int _x, int _y, int _w, int _h,
                                 GuiEngine* _guiEngine, MapHelper* _mapHelper):
PG_ThemeWidget(parent, PG_Rect(_x,_y,_w,_h), "Widget") {
//  my_parent = parent;
  guiEngine=_guiEngine;
  mapHelper=_mapHelper;

  buildButton=new PG_Button(this, PG_Rect(3,5,38,38));
  buildButton->SetIcon("graphics/icons/build_track.png");
  buildButton->SetToggle(true);

  upgradeButton=new PG_Button(this, PG_Rect(43,5,38,38));
  upgradeButton->SetIcon("graphics/icons/upgrade_track.png");
  upgradeButton->SetToggle(true);

  stationButton=new PG_Button(this, PG_Rect(83,5,38,38));
  stationButton->SetIcon("graphics/icons/build_stations.png");
  stationButton->SetToggle(true);

  removeButton=new PG_Button(this, PG_Rect(123,5,38,38));
  removeButton->SetIcon("graphics/icons/bulldozer.png");
  removeButton->SetToggle(true);

  singleTrackButton=new PG_Button(this, PG_Rect(3,45,38,38));
  singleTrackButton->SetIcon("graphics/icons/standard_track.png");
  singleTrackButton->SetToggle(true);

  doubleTrackButton=new PG_Button(this, PG_Rect(43,45,38,38));
  doubleTrackButton->SetIcon("graphics/icons/double_track.png");
  doubleTrackButton->SetToggle(true);
}

TerrainBuildPane::~TerrainBuildPane() {
}
