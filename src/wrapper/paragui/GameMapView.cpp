/*
 * $Id$
 */

#include "GameMapView.h"

#include "SDL.h"
#include "SDL_image.h"

GameMapView::GameMapView(GameMainWindow* parent, int x, int y, int w, int h, WorldMap* _worldMap):
PG_GradientWidget(parent->getWidget(), PG_Rect(x,y,w,h), "GradientWidget") {
  PG_Point p;
  SetBackgroundBlend(0);
  worldMap=_worldMap;
  sdlimage=IMG_Load("data/graphics/tiles.png");
  imageField.resize(worldMap->getWidth()*worldMap->getHeight());
  for (int y=0;y<worldMap->getHeight();y++)
  {
    for (int x=0;x<worldMap->getWidth();x++)
    {
      p.x=(x*30)+1;
      p.y=(y*30)+1;
      SDL_Surface* imageSurface=getMapImage(worldMap->getMapField(x,y)->getType());
      imageField[x+(y*worldMap->getWidth())]=new PG_Image(this, p, imageSurface,"GradientWidget");
    }
  }
}

GameMapView::~GameMapView() {

  cerr << "Blob" << endl;
  for (int y=0;y<worldMap->getHeight();y++)
  {
    for (int x=0;x<worldMap->getWidth();x++)
    {
      delete imageField[x+(y*worldMap->getWidth())];
      delete sdlimage;
    }
  }
  cerr << "Blub" << endl;

}

SDL_Surface* GameMapView::getMapImage(MapField::FieldType type) {
  SDL_Surface* surface=SDL_CreateRGBSurface(SDL_SWSURFACE,32,32,32,0,0,0,0);
  SDL_Rect rectSRC;
  rectSRC.x=2*30;
  rectSRC.y=7*30;
  rectSRC.w=30;
  rectSRC.h=30;
  SDL_Rect rectDST;
  rectDST.x=0;
  rectDST.y=0;
  rectDST.w=rectSRC.w;
  rectDST.h=rectSRC.h;
  SDL_BlitSurface(sdlimage, &rectSRC, surface, &rectDST);
  return surface;
}