/*
 * $Id$
 */

#include "GameMapView.h"

#include "pgimage.h"
#include "paragui_types.h"

#include "SDL.h"
#include "SDL_image.h"

GameMapView::GameMapView(GameMainWindow* parent, int x, int y, int w, int h, WorldMap* _worldMap):
PG_GradientWidget(parent->getWidget(), PG_Rect(x,y,w,h), "GradientWidget") {
  PG_Image* images[1000];
  SetBackgroundBlend(0);
  worldMap=_worldMap;
  PG_Point p;
  p.x=1;
  p.y=1;
  SDL_Surface* sdlimage=IMG_Load("data/graphics/tiles.png");
  SDL_Surface* greenGras=SDL_CreateRGBSurface(SDL_SWSURFACE,32,32,32,0,0,0,0);
  SDL_Rect rectSRC;
  rectSRC.x=0;
  rectSRC.y=0;
  rectSRC.w=29;
  rectSRC.h=29;
  SDL_Rect rectDST;
  rectDST.x=0;
  rectDST.y=0;
  rectDST.w=rectSRC.w;
  rectDST.h=rectSRC.h;
  SDL_BlitSurface(sdlimage, &rectSRC, greenGras, &rectDST);
  for (int y=0;y<30;y++)
  for (int x=0;x<30;x++)
  {
    p.x=x*29+1;
    p.y=y*29+1;
    images[x+(y*30)]=new PG_Image(this, p, greenGras,"GradientWidget");
  }
}

GameMapView::~GameMapView() {
}