#include "GameFrameHandler.h"

GameFrameHandler::GameFrameHandler(PG_FrameApplication* app, MapHelper* _mapHelper, WorldMap *_worldMap):
  PG_FrameHandler(app)
{
  SDL_Surface* screen = app->GetScreen();
  my_surface=SDL_CreateRGBSurface(SDL_SWSURFACE|SDL_SRCALPHA, 800, 600, screen->format->BitsPerPixel, 
  screen->format->Rmask, screen->format->Gmask, screen->format->Bmask, screen->format->Amask);
  map = _worldMap;
  mapHelper = _mapHelper;
  for (int y=0; y<map->getHeight(); y++)
  {
    for (int x=0; x<map->getWidth(); x++)
    {
      mapHelper->drawMapPixmap(x,y,my_surface,true);
      mapHelper->drawMapTrack(x,y,my_surface,true);
    }
  }
}

GameFrameHandler::~GameFrameHandler()
{
}

void GameFrameHandler::UpdateBackground(int x, int y)
{
  mapHelper->drawMapPixmap(x,y,my_surface,true);
  mapHelper->drawMapTrack(x,y,my_surface,true);
}

void GameFrameHandler::UpdateTiles(int x, int y)
{
  for (int yy=y-1;yy<=y+1;yy++)
  {
    for (int xx=x-1;xx<=x+1;xx++)
    {
      mapHelper->drawMapPixmap(xx,yy,my_surface,true);
    }
  }
  for (int yy=y-2;yy<=y+2;yy++)
  {
    for (int xx=x-2;xx<=x+2;xx++)
    {
      mapHelper->drawMapTrack(xx,yy,my_surface,true);
    }
  }
}


void GameFrameHandler::NextFrame(SDL_Surface* surface)
{
}

void GameFrameHandler::DrawBackground(SDL_Surface* surface)
{
  SDL_BlitSurface(my_surface, NULL, surface, NULL);
}
