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

GameMapView::GameMapView(GameMainWindow* parent, int x, int y, int w, int h, WorldMap* _worldMap):
PG_GradientWidget(parent->getWidget(), PG_Rect(x,y,w,h), "GradientWidget") {

  worldMap=_worldMap;
  WidgetList = new PG_WidgetList(this, PG_Rect(0,0,w,h));
  WidgetList->EnableScrollBar(true, PG_SB_VERTICAL);
  WidgetList->EnableScrollBar(true, PG_SB_HORIZONTAL);
  PG_Point p;
  SetBackgroundBlend(0);
  sdlimage=IMG_Load("data/graphics/tiles.png");
  
  SDL_Surface* imageSurface=SDL_CreateRGBSurface(SDL_SWSURFACE,worldMap->getWidth()*30,worldMap->getHeight()*30,32,0,0,0,0);
  for (int y=0;y<worldMap->getHeight();y++)
  {
    for (int x=0;x<worldMap->getWidth();x++)
    {
      getMapImage(imageSurface,x,y);
    }
  }

  p.x=0;
  p.y=0;
  view=new PG_Image(this, p, imageSurface);
  WidgetList->AddWidget(view);
  
  mouseType=0;
}

GameMapView::~GameMapView() {

  cerr << "Blob" << endl;
  delete WidgetList;
  delete sdlimage;
  cerr << "Blub" << endl;

}

void GameMapView::getMapImage(SDL_Surface* surface, int x, int y) {
  SDL_Rect rectSRC;
  rectSRC.w=30;
  rectSRC.h=30;
  MapField::FieldType type=worldMap->getMapField(x,y)->getType();
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
  SDL_BlitSurface(sdlimage, &rectSRC, surface, &rectDST);
}

int GameMapView::getImagePos(int x, int y, MapField::FieldType type)
{
  MapField* field;
  int xpos=0;
  
  field=worldMap->getMapField(x,y-1);
  if (field!=NULL && field->getType()!=type) xpos+=1;
  field=worldMap->getMapField(x+1,y);
  if (field!=NULL && field->getType()!=type) xpos+=2;
  field=worldMap->getMapField(x,y+1);
  if (field!=NULL && field->getType()!=type) xpos+=4;
  field=worldMap->getMapField(x-1,y);
  if (field!=NULL && field->getType()!=type) xpos+=8;

  return xpos;
}

int GameMapView::getRiverImagePos(int x, int y)
{
  MapField* field;
  int xpos=0;
  
  field=worldMap->getMapField(x,y-1);
  if (field!=NULL && field->getType()!=MapField::river && field->getType()!=MapField::ocean) xpos+=1;
  field=worldMap->getMapField(x+1,y);
  if (field!=NULL && field->getType()!=MapField::river && field->getType()!=MapField::ocean) xpos+=2;
  field=worldMap->getMapField(x,y+1);
  if (field!=NULL && field->getType()!=MapField::river && field->getType()!=MapField::ocean) xpos+=4;
  field=worldMap->getMapField(x-1,y);
  if (field!=NULL && field->getType()!=MapField::river && field->getType()!=MapField::ocean) xpos+=8;

  return xpos;
}

int GameMapView::get3DImagePos(int x, int y, MapField::FieldType type)
{
  MapField* fieldLeft;
  MapField* fieldRight;
  int xpos=0;
  
  fieldLeft=worldMap->getMapField(x-1,y);
  fieldRight=worldMap->getMapField(x+1,y);
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

  mouseType=type;;
}

void GameMapView::eventMouseEnter() {

  cerr << mouseType << endl;
}