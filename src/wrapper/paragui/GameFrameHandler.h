#ifndef PG_GAMEFRAMEHANDLER_H
#define PG_GAMEFRAMEHANDLER_H

#include <SDL.h>

#include "pgframeapplication.h"
#include "pgframehandler.h"
#include "pgspritebase.h"
#include "pgspriteobject.h"

#include "WorldMap.h"

class DECLSPEC GameFrameHandler : public PG_FrameHandler {
public:
	/**
	Creates a GameFrameHandler which have an own surface to draw
	an for animation/sprites
	*/
	GameFrameHandler(PG_FrameApplication* app, WorldMap *_worldMap);

	/**
	Destroys a GameFrameHandler
	*/
	virtual ~GameFrameHandler();

	/**
	The NextFrame function is called from PG_FrameApplication
	*/
	void NextFrame(SDL_Surface *surface);

	/**
	The NextFrame function is called from PG_FrameApplication
	*/
	void DrawBackground(SDL_Surface *surface);
	void UpdateBackground(int x, int y);
	void UpdateTiles(int x, int y);
	
	std::string bit2str(int bitfield, int count);

  std::string bigframe(int x, int y, MapField::FieldType type);
  std::string smallframe(int x, int y, MapField::FieldType type);
  void drawMapPixmap(int x, int y);
  void drawMapTrack(int x, int y);

protected:
	WorldMap *map;
        PG_SpriteBase terrainbase;
        PG_SpriteBase trackbase;
        PG_SpriteObject terrain;
        PG_SpriteObject track;
	SDL_Surface* my_surface;

};
#endif // GAMEFRAMEHANDLER_H
