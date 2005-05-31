#include <vector>

#include "MapField.h"

#include "TrainFrame.h"
#include "Track.h"
#include <pgfilearchive.h>


TrainFrame::TrainFrame(WorldMap* _map, Train* _train)
{
  map=_map;
  train=_train;
  wagons = train->getWagons();
  posOnTrack = 0;

  FindFirst();
  sprite = LoadImage("Grasshopper", direction);

  pos = new PG_Rect(train->getPosX()*30,train->getPosY()*30,0,0);
  for (int i=0; i<wagons.size(); i++)
  {
    positions.push_back(new SDL_Rect(*pos));
  }
}

void TrainFrame::FindFirst()
{
  MapField* field = map->getMapField(train->getPosX(), train->getPosY());
  Track* track = field->getTrack();
  if (track->getConnect()&TrackGoNorth)
  {
    direction=TrackGoNorth;
  }
  else if (track->getConnect()&TrackGoEast)
  {
    direction=TrackGoEast;
  }
  else if (track->getConnect()&TrackGoSouth)
  {
    direction=TrackGoSouth;
  }
  else if (track->getConnect()&TrackGoWest)
  {
    direction=TrackGoWest;
  }
  else if (track->getConnect()&TrackGoNorthEast)
  {
    direction=TrackGoNorthEast;
  }
  else if (track->getConnect()&TrackGoSouthEast)
  {
    direction=TrackGoSouthEast;
  }
  else if (track->getConnect()&TrackGoSouthWest)
  {
    direction=TrackGoSouthWest;
  }
  else if (track->getConnect()&TrackGoNorthWest)
  {
    direction=TrackGoNorthWest;
  }
}

void TrainFrame::FindNext()
{
  MapField* field = map->getMapField((pos->x/30), (pos->y/30));
  Track* track = field->getTrack();
  if (!(track->getConnect()&direction))
  {
    int new_direction;
    // Next left
    if (direction==TrackGoNorthWest)
    {
      new_direction=TrackGoNorth;
    } else
    {
      new_direction = direction * 2;
    }
    if (!(track->getConnect()&new_direction))
    {
      //Next right
      if (direction==TrackGoNorth)
      {
        new_direction=TrackGoNorthWest;
      } else
      {
        new_direction = direction / 2;
      }
      if (!(track->getConnect()&new_direction))
      {
        if (direction<16) { new_direction = direction * 16;} 
        else { new_direction = direction / 16;}
      }
    }
    direction=new_direction;
  }
  directions.insert(directions.begin(),direction);
  
  if (directions.size()>wagons.size()+1)
  {
    directions.pop_back();
  }
}

void TrainFrame::CalcPos(int _direction, SDL_Rect* _pos)
{
  switch(_direction)
  {
    case TrackGoNorth:
      _pos->y -= 1;
    break;
    case TrackGoNorthEast:
      _pos->x += 1;
      _pos->y -= 1;
    break;
    case TrackGoEast:
      _pos->x += 1;
    break;
    case TrackGoSouthEast:
      _pos->x += 1;
      _pos->y += 1;
    break;
    case TrackGoSouth:
      _pos->y += 1;
    break;
    case TrackGoSouthWest:
      _pos->x -= 1;
      _pos->y += 1;
    break;
    case TrackGoWest:
      _pos->x -= 1;
    break;
    case TrackGoNorthWest:
      _pos->x -= 1;
      _pos->y -= 1;
    break;
  }
}

TrainFrame::~TrainFrame()
{
}

SDL_Surface* TrainFrame::LoadImage(std::string type_str, int dir)
{
  std::string direction_str;
  switch (dir)
  {
    case TrackGoNorth:
      direction_str="s";
    break;
    case TrackGoNorthEast:
      direction_str="sw";
    break;
    case TrackGoEast:
      direction_str="w";
    break;
    case TrackGoSouthEast:
      direction_str="nw";
    break;
    case TrackGoSouth:
      direction_str="n";
    break;
    case TrackGoSouthWest:
      direction_str="ne";
    break;
    case TrackGoWest:
      direction_str="e";
    break;
    case TrackGoNorthWest:
      direction_str="se";
    break;
  }
  return PG_FileArchive::LoadSurface(("graphics/trains/overhead/"+type_str+"_"+direction_str+".png").c_str(), false);
}

void TrainFrame::DrawWagons(SDL_Surface* surface)
{
  for (int i=0; i<wagons.size(); i++)
  {
    if (directions.size()>i+1)
    {
      CalcPos(directions[i+1], positions[i]);
      sprite = LoadImage("Petroleum", directions[i+1]);
      SDL_BlitSurface(sprite, NULL, surface, positions[i]);
    }
  }
}

void TrainFrame::NextFrame(SDL_Surface* surface, Uint32 background)
{
  if (posOnTrack==0)
  {
    FindNext();
  }
  sprite = LoadImage("Grasshopper", direction);
  CalcPos(direction, pos);
  SDL_BlitSurface(sprite, NULL, surface, pos);
  DrawWagons(surface);
  posOnTrack += 1;
  if (posOnTrack==30) posOnTrack=0;
}
