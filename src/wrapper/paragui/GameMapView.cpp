/*
 * $Id$
 *
 * Need's rewrite, cause so we need to much memory!
 * I've choosen a WidgetList cause of the scroll functionality, but we should implement
 * our own Widget with 2 PG_ScrollBar's.
 * This reduce Memory usage (at the moment 53MByte by 100x100 GameWorld)
 *
 */

#include "GameMapView.h"

#include "SDL.h"
#include "SDL_image.h"

GameMapView::GameMapView(GameMainWindow* parent, int x, int y, int w, int h, Engine* _engine):
PG_ThemeWidget(parent->getWidget(), PG_Rect(x,y,w,h), "ThemeWidget") {

  engine=_engine;
  WidgetList = new PG_WidgetList(this, PG_Rect(0,0,w,h));
  WidgetList->EnableScrollBar(true, PG_SB_VERTICAL);
  WidgetList->EnableScrollBar(true, PG_SB_HORIZONTAL);
  PG_Point p;
  SetBackgroundBlend(0);
  tilesImage=IMG_Load("data/graphics/tiles.png");
  trackImage=IMG_Load("data/graphics/track.png");
  
  imageSurface=SDL_CreateRGBSurface(SDL_SWSURFACE,engine->getWorldMap()->getWidth()*30,engine->getWorldMap()->getHeight()*30,32,0,0,0,0);
  for (int y=0;y<engine->getWorldMap()->getHeight();y++)
  {
    for (int x=0;x<engine->getWorldMap()->getWidth();x++)
    {
      getMapImage(imageSurface,x,y);
    }
  }

  p.x=0;
  p.y=0;
  view=new PG_Image(this, p, imageSurface, false);
  WidgetList->AddWidget(view);
  cerr << view->GetName() << endl;
  mouseType=0;
  mouseOldX=0;
  mouseOldY=0;
}

GameMapView::~GameMapView() {

//  imageSurface=NULL;
  cerr << "Blob" << endl;
//  delete WidgetList;
  delete tilesImage;
  delete trackImage;
  cerr << "Blub" << endl;

}

void GameMapView::getMapImage(SDL_Surface* surface, int x, int y) {

  SDL_Rect rectSRC;
  rectSRC.w=30;
  rectSRC.h=30;
  MapField* field = engine->getWorldMap()->getMapField(x,y);
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
    default:
    {
      rectSRC.x=13*30;
      rectSRC.y=7*30;
    }
  }
  SDL_Rect rectDST;
  rectDST.x=x*30;
  rectDST.y=y*30;
  rectDST.w=rectSRC.w;
  rectDST.h=rectSRC.h;
  SDL_BlitSurface(tilesImage, &rectSRC, surface, &rectDST);
}

int GameMapView::getImagePos(int x, int y, MapField::FieldType type)
{
  MapField* field;
  int xpos=0;
  
  field=engine->getWorldMap()->getMapField(x,y-1);
  if (field!=NULL && field->getType()!=type) xpos+=1;
  field=engine->getWorldMap()->getMapField(x+1,y);
  if (field!=NULL && field->getType()!=type) xpos+=2;
  field=engine->getWorldMap()->getMapField(x,y+1);
  if (field!=NULL && field->getType()!=type) xpos+=4;
  field=engine->getWorldMap()->getMapField(x-1,y);
  if (field!=NULL && field->getType()!=type) xpos+=8;

  return xpos;
}

int GameMapView::getRiverImagePos(int x, int y)
{
  MapField* field;
  int xpos=0;
  
  field=engine->getWorldMap()->getMapField(x,y-1);
  if (field!=NULL && field->getType()!=MapField::river && field->getType()!=MapField::ocean) xpos+=1;
  field=engine->getWorldMap()->getMapField(x+1,y);
  if (field!=NULL && field->getType()!=MapField::river && field->getType()!=MapField::ocean) xpos+=2;
  field=engine->getWorldMap()->getMapField(x,y+1);
  if (field!=NULL && field->getType()!=MapField::river && field->getType()!=MapField::ocean) xpos+=4;
  field=engine->getWorldMap()->getMapField(x-1,y);
  if (field!=NULL && field->getType()!=MapField::river && field->getType()!=MapField::ocean) xpos+=8;

  return xpos;
}

int GameMapView::get3DImagePos(int x, int y, MapField::FieldType type)
{
  MapField* fieldLeft;
  MapField* fieldRight;
  int xpos=0;
  
  fieldLeft=engine->getWorldMap()->getMapField(x-1,y);
  fieldRight=engine->getWorldMap()->getMapField(x+1,y);
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

  getMapImage(imageSurface,mouseOldX,mouseOldY);
  Update();
}

bool GameMapView::eventMouseButtonDown(const SDL_MouseButtonEvent* button) {

  if (button->button==SDL_BUTTON_LEFT) {
    int x = button->x;
    int y = button->y;
    // doBuild(what,x,y);
  }
  return false;
}

void GameMapView::regenerateTile(int x, int y) {

  for (int i=-1;i<=1;i++) {
    for (int ii=-1;ii<=1;ii++) {
      getMapImage(imageSurface,mouseOldX+i,mouseOldY+ii);
    }
  }
  mouseOldX=x;
  mouseOldY=y;
}

void GameMapView::showTrack(int x, int y, int tilesetX, int tilesetY) {

  SDL_Rect rectSRC, rectDST;
  rectSRC.w=30;
  rectSRC.h=30;
  rectSRC.x=tilesetX;
  rectSRC.y=tilesetY;
  rectDST.x=x*30;
  rectDST.y=y*30;
  rectDST.w=rectSRC.w;
  rectDST.h=rectSRC.h;
  SDL_BlitSurface(trackImage, &rectSRC, imageSurface, &rectDST);
}

bool GameMapView::eventMouseMotion(const SDL_MouseMotionEvent* motion) {

  int x,y, x2, y2;
  int dir, dir2, helpx, helpy;
  x = motion->x / 30;
  y = motion->y / 30;
  helpx=x*30;
  helpx=motion->x-helpx;
  helpy=y*30;
  helpy=motion->y-helpy;
  
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
  
  if (dir<5) dir2=dir+4;
    else dir2=dir-4;
    
  switch (dir) {
    case 1: y2=y-1; x2=x;
    break;
    case 2: y2=y-1; x2=x+1;
    break;
    case 3: y2=y; x2=x+1;
    break;
    case 4: y2=y+1; x2=x+1;
    break;
    case 5: y2=y+1; x2=x;
    break;
    case 6: y2=y+1; x2=x-1;
    break;
    case 7: y2=y; x2=x-1;
    break;
    case 8: y2=y-1; x2=x-1;
    break;
  }

  regenerateTile(x,y);
  
  switch (mouseType) {
  
    case buildStation:
  // canBuild(Station,x,y);
      showTrack(x,y,20*30+15,26*30+15);
      break;
    case buildTrack:

// TODO      
/*      if (engine->canBuildTrack(x,y,1,dir)>=0)
      {
        showTrack(x,y,(dir-1)*2*30+15,0*30+15);
	if (engine->canBuildTrack(x2,y2,1,dir2)>=0)
	{
	  showTrack(x2,y2,(dir2-1)*2*30+15,0*30+15);
	}
      }
*/      break;
    default:
      return false;
      break;
  }
  Update();
  return true;
}
