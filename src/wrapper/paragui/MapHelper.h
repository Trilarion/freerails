#ifndef __MAPHELPER_H__
#define __MAPHELPER_H__

#include <SDL.h>

#include "pgspriteobject.h"
#include "pgspritebase.h"

#include "WorldMap.h"

class MapHelper {
public:
	/**
	*/
	MapHelper(PG_SpriteBase* terrainbase, PG_SpriteBase* trackbase, WorldMap *_worldMap);

	/**
	*/
	virtual ~MapHelper();

	std::string bit2str(int bitfield, int count);

	void drawMapPixmap(int x, int y, SDL_Surface* surface, bool drawToPos);
	void drawMapTrack(int x, int y, SDL_Surface* surface, bool drawToPos);

protected:
	WorldMap *map;
        PG_SpriteObject* terrain;
        PG_SpriteObject* track;

	std::string bigframe(int x, int y, MapField::FieldType type);
	std::string smallframe(int x, int y, MapField::FieldType type);
};

#endif // __MAPHELPER_H__
