#ifndef __TRAINFRAME_H
#define __TRAINFRAME_H

#include "pgframeobject.h"

#include "WorldMap.h"
#include "Train.h"
#include "Wagon.h"

#include <SDL.h>
#include <vector>
#include <queue>

class TrainFrame : public PG_FrameObject {
public:
	TrainFrame(WorldMap* map, Train* train);
	virtual ~TrainFrame();
	SDL_Surface* LoadImage(std::string type_str, int dir);
	void FindFirst();
	void FindNext();
	void CalcPos(int direction, SDL_Rect* pos);
	void NextFrame(SDL_Surface* surface, Uint32 background);
	void DrawWagons(SDL_Surface* surface);
private:
        WorldMap* map;
	Train* train;
	std::vector<Wagon*> wagons;
	std::vector<SDL_Rect*> positions;
	std::vector<int> directions;
	int direction;
	int posOnTrack;
};

#endif // __TRAINFRAME_H
