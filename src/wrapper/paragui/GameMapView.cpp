/*
 * $Id$
 *
 */

#include "GameMapView.h"

#include "SDL.h"
#include "SDL_image.h"

GameMapView::GameMapView(GameMainWindow* parent, int x, int y, int w, int h, GuiEngine* _guiEngine):
PG_ThemeWidget(parent->getWidget(), PG_Rect(x,y,w,h), "ThemeWidget"),
Base2DMapView(x,y,w,h,_guiEngine) {

  PG_Point p;
  
  viewPos.x=0;
  viewPos.y=0;
  
  SetBackgroundBlend(0);
  tilesImage=IMG_Load("data/graphics/tilesets/default/terrain_tiles.png");
  trackImage=IMG_Load("data/graphics/tilesets/default/track_tiles.png");
  
  imageSurface=SDL_CreateRGBSurface(SDL_SWSURFACE,w,h,32,0,0,0,0);

  p.x=0;
  p.y=0;
  view=new PG_Image(this, p, imageSurface, false);
  redrawMap(0,0,w,h);
  verticalScrollBar = new PG_ScrollBar(this,200/*ID*/,PG_Rect(w-15,0,15,h-15),PG_SB_VERTICAL);
  horizontalScrollBar = new PG_ScrollBar(this,201/*ID*/,PG_Rect(0,h-15,w-15,15),PG_SB_HORIZONTAL);
  
  verticalScrollBar->SetRange(0,(guiEngine->getWorldMap()->getHeight()*30)-h);
  verticalScrollBar->SetPageSize(h);
  verticalScrollBar->SetLineSize(30);
  horizontalScrollBar->SetRange(0,(guiEngine->getWorldMap()->getWidth()*30)-w);
  horizontalScrollBar->SetPageSize(w);
  horizontalScrollBar->SetLineSize(30);
  mouseType=0;
  mouseOldMapX=0;
  mouseOldMapY=0;
}

GameMapView::~GameMapView() {

  cerr << "Blob" << endl;
  delete tilesImage;
  delete trackImage;
  delete imageSurface;
  cerr << "Blub" << endl;

}

void GameMapView::drawMapPixmap(int mapX, int mapY) {

  int tilesetPosX, tilesetPosY;

  MapField* field = guiEngine->getWorldMap()->getMapField(mapX,mapY);
  if (field==NULL) return;
  MapField::FieldType type=field->getType();
  switch (type)
  { case MapField::grass:
    {
      tilesetPosX=0;
      tilesetPosY=0;
      break;
    }
    case MapField::dessert:
    {
      tilesetPosX=getPixmapPos(mapX,mapY,MapField::dessert);
      tilesetPosY=1;
      break;
    }
    case MapField::river:
    {
      tilesetPosX=getRiverPixmapPos(mapX,mapY);
      tilesetPosY=4;
      break;
    }
    case MapField::ocean:
    {
      tilesetPosX=getPixmapPos(mapX,mapY,MapField::ocean);
      tilesetPosY=5;
      break;
    }
    case MapField::bog:
    {
      tilesetPosX=getPixmapPos(mapX,mapY,MapField::bog);
      tilesetPosY=2;
      break;
    }
    case MapField::jungle:
    {
      tilesetPosX=getPixmapPos(mapX,mapY,MapField::jungle);
      tilesetPosY=3;
      break;
    }
    case MapField::wood:
    {
      tilesetPosX=get3DPixmapPos(mapX,mapY,MapField::wood);
      tilesetPosY=6;
      break;
    }
    case MapField::foothills:
    {
      tilesetPosX=get3DPixmapPos(mapX,mapY,MapField::foothills);
      tilesetPosY=6;
      break;
    }
    case MapField::hills:
    {
      tilesetPosX=4+get3DPixmapPos(mapX,mapY,MapField::hills);
      tilesetPosY=6;
      break;
    }
    case MapField::mountain:
    {
      tilesetPosX=8+get3DPixmapPos(x,y,MapField::mountain);
      tilesetPosY=6;
      break;
    }
/* 
  This are normaly no map type
  They should be a gameElement.
*/    case MapField::village:
    {
      // What village?
      tilesetPosX=0;
      tilesetPosY=8;
      break;
    }
    case MapField::city:
    {
      // What city?
      tilesetPosX=2;
      tilesetPosY=7;
      break;
    }
    case MapField::slum:
    {
      // What Slum?
      tilesetPosX=2;
      tilesetPosY=8;
      break;
    }
    case MapField::resource:
    {
      // What Resource?
      tilesetPosX=8;
      tilesetPosY=8;
      break;
    }
    case MapField::industrie:
    {
      // What Industrie?
      tilesetPosX=5;
      tilesetPosY=8;
      break;
    }
    case MapField::farm:
    {
      // What Farm? Have we more then 1?
      tilesetPosX=6;
      tilesetPosY=7;
      break;
    }

    default:
    {
      tilesetPosX=13;
      tilesetPosY=7;
    }
  }
  drawTilesPixmap(tilesetPosX,tilesetPosY,mapX,mapY);
}

void GameMapView::drawElementsPixmap(int mapX, int mapY) {

  MapField* field = guiEngine->getWorldMap()->getMapField(mapX,mapY);
  if (field==NULL) return;
  GameElement* element = field->getElement();
  if (element==NULL) return;
  
  switch (element->getTypeID())
  {
    case (GameElement::idStation):
      drawStationPixmap(mapX, mapY, (Station*) element);
    break;
// others like industrie there
  }
}

void GameMapView::drawTrackPixmap(int mapX, int mapY) {

  MapField* field = guiEngine->getWorldMap()->getMapField(mapX,mapY);
  if (field==NULL) return;
  Track* track = field->getTrack();
  if (track==NULL) return;
  drawTrackPixmap(mapX, mapY, track);  
}

void GameMapView::drawStationPixmap(int mapX, int mapY, Station* station) {
#warning It dont look for station type
  int tilesetX, tilesetY;
  MapField* field = guiEngine->getWorldMap()->getMapField(mapX,mapY);
  if (field == NULL) return;
  Track* track = field->getTrack();
  if (track==NULL) return;
  unsigned int connect = track->getConnect();
  
  switch (connect ^ TrackIsBlocked)
  {
    case TrackGoNorth:
    case TrackGoSouth:
    case TrackGoNorth | TrackGoSouth:
      tilesetX=18*30+15;
      tilesetY=26*30+15;
    break;
    case TrackGoNorthEast:
    case TrackGoSouthWest:
    case TrackGoNorthEast | TrackGoSouthWest:
      tilesetX=20*30+15;
      tilesetY=26*30+15;
    break;
    case TrackGoEast:
    case TrackGoWest:
    case TrackGoEast | TrackGoWest:
      tilesetX=22*30+15;
      tilesetY=26*30+15;
    break;
    case TrackGoNorthWest:
    case TrackGoSouthEast:
    case TrackGoNorthWest | TrackGoSouthEast:
      tilesetX=24*30+15;
      tilesetY=26*30+15;
    break;
  }
  drawPixmap(trackImage, tilesetX, tilesetY, mapX, mapY);
}


void GameMapView::drawTrackPixmap(int mapX, int mapY, Track* track) {

  int tilesetX, tilesetY;
  switch (track->getConnect())
  {
    case TrackGoNorth:
      tilesetX=(0)*2*30+15;
      tilesetY=0*30+15;
    break;
    case TrackGoNorthEast:
      tilesetX=(1)*2*30+15;
      tilesetY=0*30+15;
    break;
    case TrackGoEast:
      tilesetX=(2)*2*30+15;
      tilesetY=0*30+15;
    break;
    case TrackGoSouthEast:
      tilesetX=(3)*2*30+15;
      tilesetY=0*30+15;
    break;
    case TrackGoSouth:
      tilesetX=(4)*2*30+15;
      tilesetY=0*30+15;
    break;
    case TrackGoSouthWest:
      tilesetX=(5)*2*30+15;
      tilesetY=0*30+15;
    break;
    case TrackGoWest:
      tilesetX=(6)*2*30+15;
      tilesetY=0*30+15;
    break;
    case TrackGoNorthWest:
      tilesetX=(7)*2*30+15;
      tilesetY=0*30+15;
    break;

    case TrackGoNorth|TrackGoSouth:
      tilesetX=(0)*2*30+15;
      tilesetY=2*30+15;
    break;
    case TrackGoNorthEast|TrackGoSouthWest:
      tilesetX=(1)*2*30+15;
      tilesetY=2*30+15;
    break;
    case TrackGoEast|TrackGoWest:
      tilesetX=(2)*2*30+15;
      tilesetY=2*30+15;
    break;
    case TrackGoSouthEast|TrackGoNorthWest:
      tilesetX=(3)*2*30+15;
      tilesetY=2*30+15;
    break;

    case TrackGoNorth|TrackGoSouthEast:
      tilesetX=(0)*2*30+15;
      tilesetY=4*30+15;
    break;
    case TrackGoSouth|TrackGoNorthEast:
      tilesetX=(1)*2*30+15;
      tilesetY=4*30+15;
    break;
    case TrackGoEast|TrackGoSouthWest:
      tilesetX=(2)*2*30+15;
      tilesetY=4*30+15;
    break;
    case TrackGoWest|TrackGoSouthEast:
      tilesetX=(3)*2*30+15;
      tilesetY=4*30+15;
    break;
    case TrackGoSouth|TrackGoNorthWest:
      tilesetX=(4)*2*30+15;
      tilesetY=4*30+15;
    break;
    case TrackGoNorth|TrackGoSouthWest:
      tilesetX=(5)*2*30+15;
      tilesetY=4*30+15;
    break;
    case TrackGoWest|TrackGoNorthEast:
      tilesetX=(6)*2*30+15;
      tilesetY=4*30+15;
    break;
    case TrackGoEast|TrackGoNorthWest:
      tilesetX=(7)*2*30+15;
      tilesetY=4*30+15;
    break;

    case TrackGoNorth|TrackGoSouth|TrackGoSouthEast:
      tilesetX=(0)*2*30+15;
      tilesetY=6*30+15;
    break;
    case TrackGoSouth|TrackGoNorthEast|TrackGoSouthWest:
      tilesetX=(1)*2*30+15;
      tilesetY=6*30+15;
    break;
    case TrackGoEast|TrackGoWest|TrackGoSouthWest:
      tilesetX=(2)*2*30+15;
      tilesetY=6*30+15;
    break;
    case TrackGoWest|TrackGoSouthEast|TrackGoNorthWest:
      tilesetX=(3)*2*30+15;
      tilesetY=6*30+15;
    break;
    case TrackGoNorth|TrackGoSouth|TrackGoNorthWest:
      tilesetX=(4)*2*30+15;
      tilesetY=6*30+15;
    break;
    case TrackGoNorth|TrackGoNorthEast|TrackGoSouthWest:
      tilesetX=(5)*2*30+15;
      tilesetY=6*30+15;
    break;
    case TrackGoEast|TrackGoWest|TrackGoNorthEast:
      tilesetX=(6)*2*30+15;
      tilesetY=6*30+15;
    break;
    case TrackGoEast|TrackGoSouthEast|TrackGoNorthWest:
      tilesetX=(7)*2*30+15;
      tilesetY=6*30+15;
    break;

    case TrackGoNorth|TrackGoSouth|TrackGoSouthWest:
      tilesetX=(0)*2*30+15;
      tilesetY=8*30+15;
    break;
    case TrackGoWest|TrackGoNorthEast|TrackGoSouthWest:
      tilesetX=(1)*2*30+15;
      tilesetY=8*30+15;
    break;
    case TrackGoEast|TrackGoWest|TrackGoNorthWest:
      tilesetX=(2)*2*30+15;
      tilesetY=8*30+15;
    break;
    case TrackGoNorth|TrackGoSouthEast|TrackGoNorthWest:
      tilesetX=(3)*2*30+15;
      tilesetY=8*30+15;
    break;
    case TrackGoNorth|TrackGoSouth|TrackGoNorthEast:
      tilesetX=(4)*2*30+15;
      tilesetY=8*30+15;
    break;
    case TrackGoEast|TrackGoNorthEast|TrackGoSouthWest:
      tilesetX=(5)*2*30+15;
      tilesetY=8*30+15;
    break;
    case TrackGoEast|TrackGoWest|TrackGoSouthEast:
      tilesetX=(6)*2*30+15;
      tilesetY=8*30+15;
    break;
    case TrackGoSouth|TrackGoSouthEast|TrackGoNorthWest:
      tilesetX=(7)*2*30+15;
      tilesetY=8*30+15;
    break;

    case TrackGoNorth|TrackGoEast|TrackGoSouth|TrackGoWest:
      tilesetX=(0)*2*30+15;
      tilesetY=10*30+15;
    break;

    case TrackGoNorthEast|TrackGoSouthEast|TrackGoSouthWest|TrackGoNorthWest:
      tilesetX=(1)*2*30+15;
      tilesetY=10*30+15;
    break;

    default:
      tilesetX=(7)*2*30+15;
      tilesetY=2*30+15;
    break;
  }
  drawPixmap(trackImage, tilesetX, tilesetY, mapX, mapY);
  if (track->getConnect() & TrackGoNorthEast)
  {
    drawPixmap(trackImage, 3*30+15, 2*30+15, mapX+1, mapY);
  }
  if (track->getConnect() & TrackGoSouthEast)
  {
    drawPixmap(trackImage, 7*30+15, 0*30+15, mapX+1, mapY);
  }
  if (track->getConnect() & TrackGoSouthWest)
  {
    drawPixmap(trackImage, 2*30+15, 1*30+15, mapX-1, mapY);
  }
  if (track->getConnect() & TrackGoNorthWest)
  {
    drawPixmap(trackImage, 5*30+15, 2*30+15, mapX-1, mapY);
  }
}

void GameMapView::drawPixmap(SDL_Surface* pixmap, int tilesetX, int tilesetY, int mapX, int mapY) {
  SDL_Rect rectSRC, rectDST;
  rectSRC.w=30;
  rectSRC.h=30;
  rectSRC.x=tilesetX;
  rectSRC.y=tilesetY;
  rectDST.x=mapX*30-viewPos.x;
  rectDST.y=mapY*30-viewPos.y;
  rectDST.w=rectSRC.w;
  rectDST.h=rectSRC.h;
  SDL_BlitSurface(pixmap, &rectSRC, imageSurface, &rectDST);
}

void GameMapView::drawTilesPixmap(int tilesetPosX, int tilesetPosY, int mapX, int mapY) {
  drawPixmap(tilesImage, tilesetPosX*30, tilesetPosY*30, mapX, mapY);
}

void GameMapView::setMouseType(MouseType type) {

  mouseType=type;
}

void GameMapView::eventMouseLeave() {

  regenerateTile(mouseOldMapX,mouseOldMapY);
  view->Update();
}

bool GameMapView::eventMouseButtonDown(const SDL_MouseButtonEvent* button) {

  if (button->button==SDL_BUTTON_LEFT) {

    unsigned int mapx, mapy;
    int dir;  
    screen2map(button->x, button->y, &mapx, &mapy, &dir);

    switch (mouseType) {
  
      case buildStation:
        guiEngine->buildStation(mapx,mapy);
      break;
      case buildTrack:
        guiEngine->buildTrack(mapx,mapy,dir);
	guiEngine->getOtherConnectionSide(&mapx,&mapy,&dir);
	guiEngine->buildTrack(mapx,mapy,dir);
      break;
    default:
      return false;
      break;
    }
  }
  return false;
}

void GameMapView::regenerateTile(int x, int y)
{
  // Not the cleanest :-)
  
  // First Map && Buildings
  for (int y1=-1;y1<=1;y1++)
  {
    for (int x1=-1;x1<=1;x1++)
    {
      drawMapPixmap(x+x1, y+y1);
      drawElementsPixmap(x+x1, y+y1);
    }
  }

  // Then the Tracks
  for (int y1=-2;y1<=2;y1++)
  {
    for (int x1=-2;x1<=2;x1++)
    {
      drawTrackPixmap(x+x1, y+y1);
    }
  }
}

void GameMapView::showTrack(int x, int y, unsigned int dir) {
# warning Clean up code
  int tilesetX=(dir-1)*2*30+15;
  int tilesetY=0*30+15;
  drawPixmap(trackImage, tilesetX, tilesetY, x, y);
  switch (dir)
  {
    case 1:
     return;
    break;
    case 2:
      x++;
      tilesetX=3*30+15;
      tilesetY=2*30+15;
    break;
    case 3:
     return;
    break;
    case 4:
      x++;
      tilesetX=7*30+15;
      tilesetY=0*30+15;
    break;
    case 5:
     return;
    break;
    case 6:
      x--;
      tilesetX=2*30+15;
      tilesetY=1*30+15;
    break;
    case 7:
     return;
    break;
    case 8:
      x--;
      tilesetX=5*30+15;
      tilesetY=2*30+15;
    break;
  }
  drawPixmap(trackImage, tilesetX, tilesetY, x, y);
}

void GameMapView::showStation(int x, int y) {
# warning Clean up code
  int tilesetX, tilesetY;
  MapField* field = guiEngine->getWorldMap()->getMapField(x,y);
  if (field == NULL) return;
  Track* track = field->getTrack();
  if (track==NULL) return;
  unsigned int connect = track->getConnect();
  
  switch (connect)
  {
    case TrackGoNorth:
    case TrackGoSouth:
    case TrackGoNorth | TrackGoSouth:
      tilesetX=18*30+15;
      tilesetY=26*30+15;
    break;
    case TrackGoNorthEast:
    case TrackGoSouthWest:
    case TrackGoNorthEast | TrackGoSouthWest:
      tilesetX=20*30+15;
      tilesetY=26*30+15;
    break;
    case TrackGoEast:
    case TrackGoWest:
    case TrackGoEast | TrackGoWest:
      tilesetX=22*30+15;
      tilesetY=26*30+15;
    break;
    case TrackGoNorthWest:
    case TrackGoSouthEast:
    case TrackGoNorthWest | TrackGoSouthEast:
      tilesetX=24*30+15;
      tilesetY=26*30+15;
    break;
  }
  drawPixmap(trackImage, tilesetX, tilesetY, x, y);
}

bool GameMapView::eventMouseMotion(const SDL_MouseMotionEvent* motion) {

  unsigned int mousex, mousey;
  unsigned int mapx, mapy;
  int dir;
  
  regenerateTile(mouseOldMapX,mouseOldMapY);

  mousex = motion->x + viewPos.x;  // realScreenPosX
  mousey = motion->y + viewPos.y;  // realScreenPosY
  
  screen2map(mousex, mousey, &mapx, &mapy, &dir);

  mouseOldMapX = mapx;
  mouseOldMapY = mapy;

  switch (mouseType) {
  
    case buildStation:
      if(guiEngine->testBuildStation(mapx,mapy)){
	showStation(mapx, mapy);
      }
      break;
    case buildTrack:
      if(guiEngine->testBuildTrack(mapx,mapy,dir)){
        showTrack(mapx,mapy,dir);
	guiEngine->getOtherConnectionSide(&mapx,&mapy,&dir);
	showTrack(mapx,mapy,dir);
      }
     
      break;
    default:
      return false;
      break;
  }

  Update();
  return true;
}

bool GameMapView::eventScrollTrack(int id, PG_Widget* widget, unsigned long data) {

  if (id == 200)
  {
    moveYto(data);
    return true;
  }
  if (id == 201)
  {
    moveXto(data);
    return true;
  }
  return false;  
}

bool GameMapView::eventScrollPos(int id, PG_Widget* widget, unsigned long data) {

  if (id == 200)
  {
    moveYto(data);
    return true;
  }
  if (id == 201)
  {
    moveXto(data);
    return true;
  }
  return false;  
}

void GameMapView::moveXto(unsigned long pos) {

  redrawMap(pos, viewPos.y, Width(), Height());
  // TODO: Don't need full redraw any time
  viewPos.x = pos;

}

void GameMapView::moveYto(unsigned long pos) {

  redrawMap(viewPos.x, pos, Width(), Height());
  // TODO: Don't need full redraw any time
  viewPos.y = pos;

}

void GameMapView::redrawMap(int x, int y, int w, int h) {

  int countX = (w / 30)+1;
  int countY = (h / 30)+1;
  
  int startXmap = (x / 30);
  int startYmap = (y / 30);
  
  int startXsurface = - (x);
  int startYsurface = - (y);

  // First Map & Buildings  
  for (int y1=0;y1<=countY;y1++)
  {
    for (int x1=0;x1<=countX;x1++)
    {
      drawMapPixmap(x1+startXmap, y1+startYmap);
    }
  }
  
  // Then tracks
  for (int y1=0;y1<=countY;y1++)
  {
    for (int x1=0;x1<=countX;x1++)
    {
      drawTrackPixmap(x1+startXmap, y1+startYmap);
    }
  }
  view->Update(true);
}
