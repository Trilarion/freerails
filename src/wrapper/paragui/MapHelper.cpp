#include "MapHelper.h"

MapHelper::MapHelper(PG_SpriteBase* terrainbase, PG_SpriteBase* trackbase,
		     WorldMap *_worldMap)
{
  map = _worldMap;
  terrain = new PG_SpriteObject();
  track = new PG_SpriteObject();
  terrain->init(terrainbase);
  terrain->stopAnim();
  track->init(trackbase);
  track->stopAnim();
}

MapHelper::~MapHelper()
{
  delete terrain;
  delete track;
}

std::string MapHelper::bit2str(int bitfield, int count)
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

std::string MapHelper::bigframe(int x, int y, MapField::FieldType type)
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

std::string MapHelper::smallframe(int x, int y, MapField::FieldType type)
{
  MapField *field;
  int start = 0;
  if ((field=map->getMapField(x+1,y))!=NULL && field->getType()==type)
  { start+=1; }
  if ((field=map->getMapField(x-1,y))!=NULL && field->getType()==type)
  { start+=2; }
  return bit2str(start,2);
}

void MapHelper::drawMapPixmap(int x, int y, SDL_Surface* surface, bool drawToPos)
{
  if (drawToPos)
  {
    terrain->set(x*30,y*30);
  } else
  {
    terrain->set(0,0);
  }
  MapField::FieldType type;
  switch ((type=map->getMapField(x,y)->getType()))
  {
    case MapField::grass:
      terrain->setFrame("Clear_"+bigframe(x,y,MapField::grass));
    break;
    case MapField::desert:
      terrain->setFrame("Desert_"+bigframe(x,y,MapField::desert));
    break;
    case MapField::jungle:
      terrain->setFrame("Jungle_"+bigframe(x,y,MapField::jungle));
    break;
    case MapField::ocean:
      terrain->setFrame("Ocean_"+bigframe(x,y,MapField::ocean));
    break;
    case MapField::river:
      terrain->setFrame("River_"+bigframe(x,y,MapField::river));
    break;
    case MapField::foothills:
      terrain->setFrame("Foothills_"+smallframe(x,y,MapField::foothills));
    break;
    case MapField::hills:
      terrain->setFrame("Hills_"+smallframe(x,y,MapField::hills));
    break;
    case MapField::mountain:
      terrain->setFrame("Mountain_"+smallframe(x,y,MapField::mountain));
    break;
    default:
      terrain->setFrame("Clear_0000");
    break;
  }
  terrain->Draw(surface);
}

void MapHelper::drawMapTrack(int x, int y, SDL_Surface* surface, bool drawToPos)
{
  if (drawToPos)
  {
    track->set(x*30-15,y*30-15);
  } else
  {
    track->set(0,0);
  }
  unsigned int connect;
  Track *the_track = map->getMapField(x,y)->getTrack();
  if (the_track!=NULL)
  {
    connect=the_track->getConnect();
    track->setFrame("track_standard_"+bit2str(connect,8));
    track->Draw(surface);
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
      track->setFrame(type_str+bit2str(connect,8));
      track->Draw(surface);
    }
  }
}
