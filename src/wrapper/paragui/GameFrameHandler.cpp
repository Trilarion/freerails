#include "GameFrameHandler.h"

GameFrameHandler::GameFrameHandler(PG_FrameApplication* app, WorldMap *_worldMap):
  PG_FrameHandler(app)
{
  SDL_Surface* screen = app->GetScreen();
  my_surface=SDL_CreateRGBSurface(SDL_SWSURFACE|SDL_SRCALPHA, 800, 600, screen->format->BitsPerPixel, 
  screen->format->Rmask, screen->format->Gmask, screen->format->Bmask, screen->format->Amask);
  map = _worldMap;
  terrainbase.LoadDirectory("data/graphics/terrain");
  terrain.init(&terrainbase);
  terrain.stopAnim();
  trackbase.LoadDirectory("data/graphics/track");
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

void GameFrameHandler::UpdateTiles(int x, int y)
{
  for (int yy=y-1;yy<=y+1;yy++)
  {
    for (int xx=x-1;xx<=x+1;xx++)
    {
      drawMapPixmap(xx,yy);
    }
  }
  for (int yy=y-2;yy<=y+2;yy++)
  {
    for (int xx=x-2;xx<=x+2;xx++)
    {
      drawMapTrack(xx,yy);
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

std::string GameFrameHandler::bit2str(int bitfield, int count)
{
  std::string tmp_str;
  int pos=1;
  for (int i=0; i<count; i++)
  {
    if (bitfield & pos)
    {
      tmp_str = "1" + tmp_str;
    } else
    {
      tmp_str = "0" + tmp_str;
    }
    pos *= 2;
  }
  return tmp_str;
}

std::string GameFrameHandler::bigframe(int x, int y, MapField::FieldType type)
{
  MapField *field;
  int start = 0;
  if ((field=map->getMapField(x-1,y))!=NULL && field->getType()==type)
  { start += 1; }
  if ((field=map->getMapField(x,y+1))!=NULL && field->getType()==type)
  { start += 2; }
  if ((field=map->getMapField(x+1,y))!=NULL && field->getType()==type)
  { start += 4; }
  if ((field=map->getMapField(x,y-1))!=NULL && field->getType()==type)
  { start += 8; }
  return bit2str(start,4);
}

std::string GameFrameHandler::smallframe(int x, int y, MapField::FieldType type)
{
  MapField *field;
  int start = 0;
  if ((field=map->getMapField(x+1,y))!=NULL && field->getType()==type)
  { start+=1; }
  if ((field=map->getMapField(x-1,y))!=NULL && field->getType()==type)
  { start+=2; }
  return bit2str(start,2);
}

void GameFrameHandler::drawMapPixmap(int x, int y)
{
  terrain.set(x*30,y*30);
  MapField::FieldType type;
  switch ((type=map->getMapField(x,y)->getType()))
  {
    case MapField::grass:
      terrain.setFrame("Clear_"+bigframe(x,y,MapField::grass));
    break;
    case MapField::desert:
      terrain.setFrame("Desert_"+bigframe(x,y,MapField::desert));
    break;
    case MapField::jungle:
      terrain.setFrame("Jungle_"+bigframe(x,y,MapField::jungle));
    break;
    case MapField::ocean:
      terrain.setFrame("Ocean_"+bigframe(x,y,MapField::ocean));
    break;
    case MapField::river:
      terrain.setFrame("River_"+bigframe(x,y,MapField::river));
    break;
    case MapField::foothills:
      terrain.setFrame("Foothills_"+smallframe(x,y,MapField::foothills));
    break;
    case MapField::hills:
      terrain.setFrame("Hills_"+smallframe(x,y,MapField::hills));
    break;
    case MapField::mountain:
      terrain.setFrame("Mountain_"+smallframe(x,y,MapField::mountain));
    break;
    default:
      terrain.setFrame("Clear_0000");
    break;
  }
  terrain.Draw(my_surface);
}

void GameFrameHandler::drawMapTrack(int x, int y)
{
  track.set(x*30-15,y*30-15);
  unsigned int connect;
  Track *the_track = map->getMapField(x,y)->getTrack();
  if (the_track!=NULL)
  {
    connect=the_track->getConnect();
    track.setFrame("track_standard_"+bit2str(connect,8));
    track.Draw(my_surface);
    Station *the_station = map->getMapField(x,y)->getStation();
    if(the_station!=NULL)
    {
      std::string type_str="";
      switch (the_station->getSize())
      {
        case Station::Signal: type_str = "signal_tower_";
	break;
        case Station::Small: type_str = "depot_";
	break;
        case Station::Medium: type_str = "station_";
	break;
        case Station::Big: type_str = "terminal_";
	break;
      }
      track.setFrame(type_str+bit2str(connect,8));
      track.Draw(my_surface);
    }
  }
}
