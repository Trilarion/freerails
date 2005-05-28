/*
 * $Id$
 *
 */

#include "Base2DMapView.h"

Base2DMapView::Base2DMapView(GuiEngine* _guiEngine):
BaseWidget() {

  # warning All calculations are based on 30x30 tileset!
  guiEngine=_guiEngine;

}

Base2DMapView::~Base2DMapView() {

}

// Old dead code for the tiles on one map.
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
  *mapX = screenX / 30;  // realMapPosX
  *mapY = screenY / 30;  // realMapPosY
}