#include "GameFrameHandler.h"

GameFrameHandler::GameFrameHandler(PG_FrameApplication* app, WorldMap *_worldMap):
  PG_FrameHandler(app)
{
  SDL_Surface* screen = app->GetScreen();
  my_surface=SDL_CreateRGBSurface(SDL_SWSURFACE|SDL_SRCALPHA, 800, 600, screen->format->BitsPerPixel, 
  screen->format->Rmask, screen->format->Gmask, screen->format->Bmask, screen->format->Amask);
  map = _worldMap;
  terrainbase.init("data/graphics/terrain");
  terrain.init(&terrainbase);
  terrain.stopAnim();
  trackbase.init("data/graphics/track");
  track.init(&trackbase);
  track.stopAnim();
  for (int y=0; y<map->getHeight(); y++)
  {
    for (int x=0; x<map->getWidth(); x++)
    {
      drawMapPixmap(x,y);
      drawMapTrack(x,y);
    }
  }
}

GameFrameHandler::~GameFrameHandler()
{
}

void GameFrameHandler::UpdateBackground(int x, int y)
{
  drawMapPixmap(x,y);
  drawMapTrack(x,y);
}


void GameFrameHandler::NextFrame(SDL_Surface* surface)
{
}

void GameFrameHandler::DrawBackground(SDL_Surface* surface)
{
  SDL_BlitSurface(my_surface, NULL, surface, NULL);
}

int GameFrameHandler::bigframe(int x, int y, int start, MapField::FieldType type)
{
  MapField *field;
  if ((field=map->getMapField(x-1,y))!=NULL && field->getType()==type)
  { start+=1; }
  if ((field=map->getMapField(x,y+1))!=NULL && field->getType()==type)
  { start+=2; }
  if ((field=map->getMapField(x+1,y))!=NULL && field->getType()==type)
  { start+=4; }
  if ((field=map->getMapField(x,y-1))!=NULL && field->getType()==type)
  { start+=8; }
  return start;
}

int GameFrameHandler::smallframe(int x, int y, int start, MapField::FieldType type)
{
  MapField *field;
  if ((field=map->getMapField(x+1,y))!=NULL && field->getType()==type)
  { start+=1; }
  if ((field=map->getMapField(x-1,y))!=NULL && field->getType()==type)
  { start+=2; }
  return start;
}

void GameFrameHandler::drawMapPixmap(int x, int y)
{
  terrain.set(x*30,y*30);
  MapField::FieldType type;
  switch ((type=map->getMapField(x,y)->getType()))
  {
    case MapField::grass:
      terrain.setFrame(0);
    break;
    case MapField::desert:
      terrain.setFrame(bigframe(x,y,16,type));
    break;
    case MapField::jungle:
      terrain.setFrame(bigframe(x,y,32,type));
    break;
    case MapField::ocean:
      terrain.setFrame(bigframe(x,y,48,type));
    break;
    case MapField::river:
      terrain.setFrame(bigframe(x,y,64,type));
    break;
    case MapField::foothills:
      terrain.setFrame(smallframe(x,y,80,type));
    break;
    case MapField::hills:
      terrain.setFrame(smallframe(x,y,84,type));
    break;
    case MapField::mountain:
      terrain.setFrame(smallframe(x,y,88,type));
    break;
    default:
      terrain.setFrame(0);
    break;
  }
  terrain.draw(my_surface);
}

void GameFrameHandler::drawMapTrack(int x, int y)
{
  track.set(x*30,y*30);
  unsigned int connect;
  switch ((connect=map->getMapField(x,y)->getTrack()->getConnect()))
  {
  }
}