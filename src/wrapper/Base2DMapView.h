/*
 * $Id$
 */

#ifndef __BASE2DMAPVIEW_H__
#define __BASE2DMAPVIEW_H__

#include "WorldMap.h"
#include "GuiEngine.h"
#include "MapField.h"
#include "BaseWidget.h"

class Base2DMapView: public BaseWidget {

  public:
  
    enum MouseType {normal=0,
                    buildTrack=10, buildStation, buildSignal};
    /**  */
    Base2DMapView(int x, int y, int w, int h, GuiEngine* _guiEngine);
    /**  */
    ~Base2DMapView();
    
  protected:
    GuiEngine* guiEngine;
    
//    void getMapImage(SDL_Surface* surface, int offsetX, int offsetY, int x, int y);
    int getPixmapPos(int x, int y, MapField::FieldType type);
    int getRiverPixmapPos(int x, int y);
    int get3DPixmapPos(int x, int y, MapField::FieldType type);
    
    void screen2map(int screenX, int screenY, unsigned int* mapX, unsigned int* mapY, int* dir);
    
};

#endif
