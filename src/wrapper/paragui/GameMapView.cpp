/*
 * $Id$
 *
 */

#include "GameMapView.h"

#include "SDL.h"
#include "SDL_image.h"

GameMapView::GameMapView(GameMainWindow* parent, int x, int y, int w, int h, GuiEngine* _guiEngine):
PG_ThemeWidget(parent->getWidget(), PG_Rect(x,y,w,h), "ThemeWidget") {

  guiEngine=_guiEngine;

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

void GameMapView::getMapImage(SDL_Surface* surface, int offsetX, int offsetY, int x, int y) {

  SDL_Rect rectSRC;
  rectSRC.w=30;
  rectSRC.h=30;
  MapField* field = guiEngine->getWorldMap()->getMapField(x,y);
  if (field==NULL) return;
  MapField::FieldType type=field->getType();
  MapField* otherField;
  int xpos=0;
  switch (type)
  { case MapField::grass:
    {
      rectSRC.x=0*30;
      rectSRC.y=0*30;
      break;
    }
    case MapField::dessert:
    {
      xpos=getImagePos(x,y,MapField::dessert);
      rectSRC.x=xpos*30;
      rectSRC.y=1*30;
      break;
    }
    case MapField::river:
    {
      xpos=getRiverImagePos(x,y);
      rectSRC.x=xpos*30;
      rectSRC.y=4*30;
      break;
    }
    case MapField::ocean:
    {
      xpos=getImagePos(x,y,MapField::ocean);
      rectSRC.x=xpos*30;
      rectSRC.y=5*30;
      break;
    }
    case MapField::bog:
    {
      xpos=getImagePos(x,y,MapField::bog);
      rectSRC.x=xpos*30;
      rectSRC.y=2*30;
      break;
    }
    case MapField::jungle:
    {
      xpos=getImagePos(x,y,MapField::jungle);
      rectSRC.x=xpos*30;
      rectSRC.y=3*30;
      break;
    }
    case MapField::wood:
    {
      xpos=get3DImagePos(x,y,MapField::wood);
      rectSRC.x=(12+xpos)*30;
      rectSRC.y=6*30;
      break;
    }
    case MapField::foothills:
    {
      xpos=get3DImagePos(x,y,MapField::foothills);
      rectSRC.x=(0+xpos)*30;
      rectSRC.y=6*30;
      break;
    }
    case MapField::hills:
    {
      xpos=get3DImagePos(x,y,MapField::hills);
      rectSRC.x=(4+xpos)*30;
      rectSRC.y=6*30;
      break;
    }
    case MapField::mountain:
    {
      xpos=get3DImagePos(x,y,MapField::mountain);
      rectSRC.x=(8+xpos)*30;
      rectSRC.y=6*30;
      break;
    }
    case MapField::village:
    {
      // What village?
      rectSRC.x=0*30;
      rectSRC.y=8*30;
      break;
    }
    case MapField::city:
    {
      // What city?
      rectSRC.x=2*30;
      rectSRC.y=7*30;
      break;
    }
    case MapField::slum:
    {
      // What Slum?
      rectSRC.x=2*30;
      rectSRC.y=8*30;
      break;
    }
    case MapField::resource:
    {
      // What Resource?
      rectSRC.x=8*30;
      rectSRC.y=8*30;
      break;
    }
    case MapField::industrie:
    {
      // What Industrie?
      rectSRC.x=5*30;
      rectSRC.y=8*30;
      break;
    }
    case MapField::farm:
    {
      // What Farm? Have we more then 1?
      rectSRC.x=6*30;
      rectSRC.y=7*30;
      break;
    }
    default:
    {
      rectSRC.x=13*30;
      rectSRC.y=7*30;
    }
  }
  SDL_Rect rectDST;
  rectDST.x=x*30+offsetX;
  rectDST.y=y*30+offsetY;
  rectDST.w=rectSRC.w;
  rectDST.h=rectSRC.h;
  SDL_BlitSurface(tilesImage, &rectSRC, surface, &rectDST);
}

int GameMapView::getImagePos(int x, int y, MapField::FieldType type)
{
  MapField* field;
  int xpos=0;
  
  field=guiEngine->getWorldMap()->getMapField(x,y-1);
  if (field!=NULL && field->getType()!=type) xpos+=1;
  field=guiEngine->getWorldMap()->getMapField(x+1,y);
  if (field!=NULL && field->getType()!=type) xpos+=2;
  field=guiEngine->getWorldMap()->getMapField(x,y+1);
  if (field!=NULL && field->getType()!=type) xpos+=4;
  field=guiEngine->getWorldMap()->getMapField(x-1,y);
  if (field!=NULL && field->getType()!=type) xpos+=8;

  return xpos;
}

int GameMapView::getRiverImagePos(int x, int y)
{
  MapField* field;
  int xpos=0;
  
  field=guiEngine->getWorldMap()->getMapField(x,y-1);
  if (field!=NULL && field->getType()!=MapField::river && field->getType()!=MapField::ocean) xpos+=1;
  field=guiEngine->getWorldMap()->getMapField(x+1,y);
  if (field!=NULL && field->getType()!=MapField::river && field->getType()!=MapField::ocean) xpos+=2;
  field=guiEngine->getWorldMap()->getMapField(x,y+1);
  if (field!=NULL && field->getType()!=MapField::river && field->getType()!=MapField::ocean) xpos+=4;
  field=guiEngine->getWorldMap()->getMapField(x-1,y);
  if (field!=NULL && field->getType()!=MapField::river && field->getType()!=MapField::ocean) xpos+=8;

  return xpos;
}

int GameMapView::get3DImagePos(int x, int y, MapField::FieldType type)
{
  MapField* fieldLeft;
  MapField* fieldRight;
  int xpos=0;
  
  fieldLeft=guiEngine->getWorldMap()->getMapField(x-1,y);
  fieldRight=guiEngine->getWorldMap()->getMapField(x+1,y);
  if (fieldLeft!=NULL && fieldRight!=NULL)
  {
    if (fieldLeft->getType()==type && fieldRight->getType()==type)
    { xpos=2;
    } else
    if (fieldRight->getType()==type)
    { xpos=1;
    } else
    if (fieldLeft->getType()==type)
    { xpos=3;
    } 
  } else
  if (fieldLeft==NULL)
  { if (fieldRight->getType()==type)
    { xpos=2;
    } else xpos=3;
  } else
  if (fieldRight==NULL)
  { if (fieldLeft->getType()==type)
    { xpos=2;
    } else xpos=1;
  }

  return xpos;
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
    int x = button->x;
    int y = button->y;
    // doBuild(what,x,y);
  }
  return false;
}

void GameMapView::regenerateTile(int x, int y)
{
  // Not the cleanest :-)
  for (int y1=-1;y1<=1;y1++)
  {
    for (int x1=-1;x1<=1;x1++)
    {
      getMapImage(imageSurface, -(viewPos.x), -(viewPos.y), x+x1, y+y1);
    }
  }
}

void GameMapView::showTrack(int x, int y, int tilesetX, int tilesetY) {

  SDL_Rect rectSRC, rectDST;
  rectSRC.w=30;
  rectSRC.h=30;
  rectSRC.x=tilesetX;
  rectSRC.y=tilesetY;
  rectDST.x=x*30-viewPos.x;
  rectDST.y=y*30-viewPos.y;
  rectDST.w=rectSRC.w;
  rectDST.h=rectSRC.h;
  SDL_BlitSurface(trackImage, &rectSRC, imageSurface, &rectDST);
}

bool GameMapView::eventMouseMotion(const SDL_MouseMotionEvent* motion) {

  unsigned int x,y;
  int dir, helpx, helpy;
  
  regenerateTile(mouseOldMapX,mouseOldMapY);

  x = motion->x + viewPos.x;  // realScreenPosX
  y = motion->y + viewPos.y;  // realScreenPosY
  
  // Should be replaced with function in upper class
  // All calculations are for a 30x30 tileset
  helpx=x % 30;// for calculate the direction on the tile
  helpy=y % 30;
  x = x / 30;  // realMapPosX
  y = y / 30;  // realMapPosY

  // now calculate direction
  if (helpx<10) {
    if (helpy<10) { dir=8; }
    else if (helpy<20) { dir=7; }
    else dir=6;
  } else
  if (helpx<20) {
    if (helpy<10) { dir=1; }
    else if (helpy<20) { dir=1; }
    else dir=5;
  } else
  {
    if (helpy<10) { dir=2; }
    else if (helpy<20) { dir=3; }
    else dir=4;
  };
  // End of to be replace

  mouseOldMapX = x;
  mouseOldMapY = y;

  switch (mouseType) {
  
    case buildStation:
      cerr << "BuildStation" << endl;
      if(guiEngine->testBuildStation(x,y)){
	showTrack(x,y,20*30+15,26*30+15);
      }
      break;
    case buildTrack:
      cerr << "BuildTrack" << endl;
      if(guiEngine->testBuildTrack(x,y,dir)){
        showTrack(x,y,(dir-1)*2*30+15,0*30+15);
	guiEngine->getOtherConnectionSide(&x,&y,&dir);
	showTrack(x,y,(dir-1)*2*30+15,0*30+15);
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
  
  for (int y1=0;y1<=countY;y1++)
  {
    for (int x1=0;x1<=countX;x1++)
    {
      getMapImage(imageSurface,startXsurface, startYsurface, x1+startXmap, y1+startYmap);
    }
  }
  view->Update(true);
}
