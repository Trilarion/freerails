/*
 * $Id$
 *
 */

#include "Base2DMapView.h"

#include "SDL.h"
#include "SDL_image.h"

Base2DMapView::Base2DMapView(int x, int y, int w, int h, GuiEngine* _guiEngine):
BaseWidget() {

  # warning All calculations are based on 30x30 tileset!
  guiEngine=_guiEngine;

}

Base2DMapView::~Base2DMapView() {

}

int Base2DMapView::getPixmapPos(int x, int y, MapField::FieldType type)
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

int Base2DMapView::getRiverPixmapPos(int x, int y)
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

int Base2DMapView::get3DPixmapPos(int x, int y, MapField::FieldType type)
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

void Base2DMapView::screen2map(int screenX, int screenY, unsigned int* mapX, unsigned int* mapY, int* dir)
{
  int helpx, helpy;
  helpx=screenX % 30;// for calculate the direction on the tile
  helpy=screenY % 30;
  *mapX = screenX / 30;  // realMapPosX
  *mapY = screenY / 30;  // realMapPosY

  // now calculate direction
  if (helpx<10) {
    if (helpy<10) { *dir=8; }
    else if (helpy<20) { *dir=7; }
    else *dir=6;
  } else
  if (helpx<20) {
    if (helpy<10) { *dir=1; }
    else if (helpy<20) { *dir=1; }
    else *dir=5;
  } else
  {
    if (helpy<10) { *dir=2; }
    else if (helpy<20) { *dir=3; }
    else *dir=4;
  };
  // End of to be replace
}
