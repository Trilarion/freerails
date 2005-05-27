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
#include "MapHelper.h"

class TerrainBuildPane: public PG_ThemeWidget {

  public:
    /**  */
    TerrainBuildPane(PG_Widget* parent, int _x, int _y, int _w, int _h,
                    GuiEngine* _engine, MapHelper* _mapHelper);
    /**  */
    ~TerrainBuildPane();

  private:

    GuiEngine* guiEngine;
    MapHelper* mapHelper;

    PG_Button* buildButton;
    PG_Button* upgradeButton;
    PG_Button* stationButton;
    PG_Button* removeButton;

    PG_Button* singleTrackButton;
    PG_Button* doubleTrackButton;
};

#endif // __TERRAINBUILDPANE_H__
