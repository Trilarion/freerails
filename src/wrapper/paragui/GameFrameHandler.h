#ifndef __GAMEFRAMEHANDLER_H__
#define __GAMEFRAMEHANDLER_H__

#include <SDL.h>

#include "pgframeapplication.h"
#include "pgframehandler.h"
#include "pgspritebase.h"
#include "pgspriteobject.h"

#include "WorldMap.h"
#include "MapHelper.h"

class DECLSPEC GameFrameHandler : public PG_FrameHandler {
public:
	/**
	Creates a GameFrameHandler which have an own surface to draw
	an for animation/sprites
	*/
	GameFrameHandler(PG_FrameApplication* app, MapHelper* _mapHelper, WorldMap *_worldMap);

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
	
protected:
	WorldMap *map;
	MapHelper* mapHelper;
	SDL_Surface* my_surface;

};
#endif // __GAMEFRAMEHANDLER_H__
