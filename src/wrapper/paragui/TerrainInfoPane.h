/*
 * $Id$
 */

#ifndef __TERRAININFOPANE_H__
#define __TERRAININFOPANE_H__

#include "GameMainWindow.h"

#include <pgthemewidget.h>
#include <pgimage.h>
#include <pglabel.h>

#include "GuiEngine.h"
#include "MapHelper.h"

class TerrainInfoPane: public PG_ThemeWidget {

  public:
    /**  */
    TerrainInfoPane(PG_Widget* parent, int _x, int _y, int _w, int _h,
                    GuiEngine* _engine, MapHelper* _mapHelper);
    /**  */
    ~TerrainInfoPane();

    void setSelected(unsigned int mapx, unsigned int mapy);

  private:

    SDL_Surface* newSurface();
    PG_Image* image;
    GuiEngine* guiEngine;
    MapHelper* mapHelper;
    PG_Image* terrainImage;
    PG_Label* terrainType;
};

#endif // __TERRAININFOPANE_H__
