/*
 * $Id$
 */

#include "TerrainInfoPane.h"

TerrainInfoPane::TerrainInfoPane(PG_Widget* parent, int _x, int _y, int _w, int _h,
                                 GuiEngine* _guiEngine, MapHelper* _mapHelper):
PG_ThemeWidget(parent, PG_Rect(_x,_y,_w,_h), "Widget") {
//  my_parent = parent;
  guiEngine=_guiEngine;
  mapHelper=_mapHelper;
  terrainImage = new PG_Image(this, PG_Point(10,10), newSurface());
  terrainType = new PG_Label(this, PG_Rect(45,15,100,20));
  setSelected(1,1);
}

TerrainInfoPane::~TerrainInfoPane() {
}

SDL_Surface* TerrainInfoPane::newSurface() {

  SDL_Surface* screen = PG_Application::GetScreen();
  SDL_Surface* my_surface=SDL_CreateRGBSurface(SDL_SWSURFACE|SDL_SRCALPHA, 30, 30, screen->format->BitsPerPixel, 
  screen->format->Rmask, screen->format->Gmask, screen->format->Bmask, screen->format->Amask);
  return my_surface;
}

void TerrainInfoPane::setSelected(unsigned int mapx, unsigned int mapy) {
  SDL_Surface* my_surface = newSurface();
  mapHelper->drawMapPixmap(mapx, mapy, my_surface, false);
  terrainImage->SetImage(my_surface);
  terrainType->SetText("Grass");
}
