/*
 * $Id$
 *
 */

#include "GameMapView.h"

#include "SDL.h"
#include "SDL_image.h"

GameMapView::GameMapView(PG_FrameApplication* _app, int x, int y, int w, int h):
PG_Widget(NULL, PG_Rect(x,y,w,h)),
Base2DMapView(NULL) {
  viewPos.x=0;
  viewPos.y=0;
  selectedX = 0;
  selectedY = 0;
  app=_app;
  selectedbase = new PG_SpriteBase();
  selectedbase->LoadDirectory("data/graphics/ui/cursors");
  selectedsprite = new PG_SpriteObject();
  selectedsprite->init(selectedbase);
  selectedsprite->startAnim();
  selectedsprite->set(0,0);
  selectedsprite->setSpeed(1);
}

GameMapView::~GameMapView() {
}

void GameMapView::setGuiEngine(GuiEngine* _guiEngine) {
  guiEngine=_guiEngine;
  app->GetFrameHandler()->AddFrameObject(selectedsprite);
}

void GameMapView::setMouseType(MouseType type) {
  mouseType=type;
}

void GameMapView::setStationType(Station::Size type) {
  stationType=type;
}

void GameMapView::eventMouseLeave() {
  std::cerr << "leave map" << std::endl;
  PG_Widget::eventMouseLeave();
}

void GameMapView::eventMouseEnter() {
  std::cerr << "enter map" << std::endl;
  PG_Widget::eventMouseEnter();
}

bool GameMapView::eventMouseButtonDown(const SDL_MouseButtonEvent* button) {
  std::cerr << "mouse down map" << std::endl;


  if (button->button==SDL_BUTTON_LEFT) {

    unsigned int mapx, mapy;
    int dir;  
    screen2map(button->x+viewPos.x, button->y+viewPos.y, &mapx, &mapy, &dir);
    switch (mouseType) {
	
    case buildStation:
	guiEngine->buildStation(mapx,mapy,stationType);
	break;
    case buildTrack:
	if(adjacentTile(mapx, mapy, &dir)){
	    guiEngine->buildTrack(selectedX,selectedY,dir);
	}
	break;
    case buildTrain:
        guiEngine->buildTrain(mapx,mapy);
	break;
    default:
    break;
    }
    drawSelected(mapx, mapy);
    return true;
  }
  return false;
}


bool GameMapView::eventMouseMotion(const SDL_MouseMotionEvent* motion) {

  std::cerr << "mouse move map" << std::endl;
  return true;
}

bool GameMapView::adjacentTile(int mapX, int mapY, int *dir) {

	int x = selectedX - mapX;
	int y = selectedY - mapY;

	if(((x <= 1) && (x >= -1)) &&
	   ((y <= 1) && (y >= -1))){

	    if(x == 0){
		if(y == 0)
		    return false;
		else if(y == 1)
		    *dir = 1; /* NORTH */
		else 
		    *dir = 5; /* SOUTH */
	    }else if(x == 1){
		if(y == 0)
		    *dir = 7; /* WEST */
		else if(y == 1)
		    *dir = 8; /* NORTHWEST */
		else
		    *dir = 6; /* SOUTHWEST */
	    }else{ 
		if(y == 0)
		    *dir = 3; /* EAST */
		else if(y == 1)
		    *dir = 2; /* NORTHEAST */
		else
		    *dir = 4; /* SOUTHEAST */
	    }
	    return true;
	}
    return false;
}

void GameMapView::drawSelected(int mapX, int mapY) {

    selectedX = mapX;
    selectedY = mapY;

    int viewX = selectedX * 30 - viewPos.x;
    int viewY = selectedY * 30 - viewPos.y;
    selectedsprite->set(viewX, viewY);
}
