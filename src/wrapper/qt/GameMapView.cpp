/** $Id$
  * View a QCanvas class
  */

#include <qpainter.h>
#include <qpixmap.h>

#include "Engine.h"
#include "GameMainWindow.h"
#include "GameMapView.h"
#include "GameWidget.h"
#include "Message.h"
#include "stationcontroller.h"
#include "Track.h"
#include "trackcontroller.h"

GameMapView::GameMapView(Engine *_engine, GameMap *_map, GameMainWindow* parent, const char* name)
           : QCanvasView((QCanvas*)_map, parent->getWidget(), name,
               Qt::WStyle_Customize | Qt::WStyle_NoBorder)
{
  engine = _engine;
  map = _map;
  setFrameShape(QFrame::NoFrame);
  setBackgroundMode(Qt::NoBackground);
  resize(parent->getWidget()->width() - 176, parent->getWidget()->height());
  

  bool status;
  
  pixTiles = new QPixmap();
  CHECK_PTR(pixTiles);
  status = pixTiles->load("data/graphics/tilesets/default/terrain_tiles.png");
  if(!status)
    qDebug("konnte terrain_tiles.png nicht laden");
    
  pixTrack = new QPixmap();
  CHECK_PTR(pixTrack);
  status = pixTrack->load("data/graphics/tilesets/default/track_tiles.png");
  if(!status)
    qDebug("konnte track_tiles.png nicht laden");

  mouseType = normal;
  mouseButton = none;
  oldMousePos.setX(0);
  oldMousePos.setY(0);
  bShowGrid = false;
}

GameMapView::~GameMapView()
{
}

void GameMapView::getMapPixmap(QPixmap *pixPaint, int x, int y)
{
  MapField *field;

  field = engine->getWorldMap()->getMapField(x, y);
  if (field == NULL)
    return;
  MapField::FieldType type = field->getType();
    
  int xpos=0;
  int ox, oy;

  switch (type)
  {
    case MapField::grass:
      ox = 0 * 30;
      oy = 0 * 30;
      break;
    case MapField::dessert:
      xpos = getPixmapPos(x, y, MapField::dessert);
      ox = xpos * 30;
      oy = 1 * 30;
      break;
    case MapField::river:
      xpos = getRiverPixmapPos(x, y);
      ox = xpos * 30;
      oy = 4 * 30;
      break;
    case MapField::ocean:
      xpos = getPixmapPos(x, y, MapField::ocean);
      ox = xpos * 30;
      oy = 5 * 30;
      break;
    case MapField::bog:
      xpos = getPixmapPos(x, y, MapField::bog);
      ox = xpos * 30;
      oy = 2 * 30;
      break;
    case MapField::jungle:
      xpos = getPixmapPos(x, y, MapField::jungle);
      ox = xpos * 30;
      oy = 3 * 30;
      break;
    case MapField::wood:
      xpos = get3DPixmapPos(x, y, MapField::wood);
      ox = (12 + xpos) * 30;
      oy = 6 * 30;
      break;
    case MapField::foothills:
      xpos = get3DPixmapPos(x, y, MapField::foothills);
      ox = (0 + xpos) * 30;
      oy = 6 * 30;
      break;
    case MapField::hills:
      xpos = get3DPixmapPos(x, y, MapField::hills);
      ox = (4 + xpos) * 30;
      oy = 6 * 30;
      break;
    case MapField::mountain:
      xpos = get3DPixmapPos(x, y, MapField::mountain);
      ox = (8 + xpos) * 30;
      oy = 6 * 30;
      break;
    default:
      ox = 13 * 30;
      oy = 7 * 30;
  }
  
  bitBlt(pixPaint, 0, 0, pixTiles, ox, oy, 30, 30, Qt::CopyROP, true);

  Track *trk;
  trk = field->getTrack();
  if (trk != NULL)
  {
    int tracktileX, tracktileY, i;
    for(i=0;i<5;i++)
    {
      trk->getTrackTile(i, &tracktileX, &tracktileY);
      if (tracktileX >= 0)
        bitBlt(pixPaint, 0, 0, pixTrack, tracktileX, tracktileY, 30, 30, Qt::CopyROP, false);
    }
  }
  #warning complete me: stations missing
}

int GameMapView::getPixmapPos(int x, int y, MapField::FieldType type)
{
  MapField* field;
  int xpos=0;

  field = engine->getWorldMap()->getMapField(x, y - 1);
  if ((field != NULL) && (field->getType() != type))
    xpos++;

  field = engine->getWorldMap()->getMapField(x + 1, y);
  if ((field != NULL) && (field->getType() != type))
    xpos += 2;

  field = engine->getWorldMap()->getMapField(x, y + 1);
  if ((field != NULL) && (field->getType() != type))
    xpos += 4;

  field = engine->getWorldMap()->getMapField(x - 1, y);
  if ((field != NULL) && (field->getType() != type))
    xpos += 8;

  return xpos;
}

int GameMapView::getRiverPixmapPos(int x, int y)
{
  MapField* field;
  int xpos=0;

  field = engine->getWorldMap()->getMapField(x, y - 1);
  if ((field != NULL) && (field->getType() != MapField::river) && (field->getType() != MapField::ocean))
    xpos++;

  field = engine->getWorldMap()->getMapField(x + 1, y);
  if ((field != NULL) && (field->getType() != MapField::river) && (field->getType() != MapField::ocean))
    xpos += 2;

  field = engine->getWorldMap()->getMapField(x, y + 1);
  if ((field != NULL) && (field->getType() != MapField::river) && (field->getType() != MapField::ocean))
    xpos += 4;

  field = engine->getWorldMap()->getMapField(x - 1, y);
  if ((field != NULL) && (field->getType() != MapField::river) && (field->getType() != MapField::ocean))
    xpos += 8;

  return xpos;
}

int GameMapView::get3DPixmapPos(int x, int y, MapField::FieldType type)
{
  MapField* fieldLeft;
  MapField* fieldRight;
  int xpos=0;

  fieldLeft = engine->getWorldMap()->getMapField(x - 1, y);
  fieldRight = engine->getWorldMap()->getMapField(x + 1, y);
  
  if ((fieldLeft != NULL) && (fieldRight != NULL))
  {
    if ((fieldLeft->getType() == type) && (fieldRight->getType() == type))
    {
      xpos = 2;
    }
    else
    {
      if (fieldRight->getType() == type)
      {
        xpos = 1;
      }
      else
      {
        if (fieldLeft->getType() == type)
          xpos = 3;
      }
    }
  }
  else
  {
    if (fieldLeft == NULL)
    {
      if (fieldRight->getType() == type)
        xpos = 2;
      else
        xpos = 3;
    }
    else
    {
      if (fieldRight == NULL)
      {
        if (fieldLeft->getType() == type)
          xpos = 2;
        else
          xpos = 1;
      }
    }
  }
  
  return xpos;
}

void GameMapView::contentsMousePressEvent(QMouseEvent* e)
{
  if(e->button() == Qt::LeftButton)
  {
    mouseButton = left;
    #warning complete me
  }

  if(e->button() == Qt::RightButton)
  {
    mouseButton = right;
    oldMousePos2 = e->pos();
  }
}

void GameMapView::contentsMouseReleaseEvent(QMouseEvent *)
{
  mouseButton = none;
}

void GameMapView::contentsMouseMoveEvent(QMouseEvent *e)
{
  if(mouseButton == right)
  {
    QPoint diff = e->pos() - oldMousePos2;
//    if(diff.x() > 0)
//    {
//    }
//    else
//    {
//      if(contentsX() == 0)
//        diff.setX(0);
//    }
//    if(diff.y() > 0)
//    {
//    }
//    else
//    {
//      if(contentsY() == 0)
//        diff.setY(0);
//    }
    if(!diff.isNull())
    {
      scrollBy(diff.x(), diff.y());
      oldMousePos2 = e->pos();
    }
  }

  if(mouseButton == left)
  {
    Message *msg;
    int x, y;

    #warning complete me
    switch(mouseType)
    {
      case buildStation:
      {
        struct station_data *trd;
        trd = new struct station_data;
        x = oldMousePos.x();
        y = oldMousePos.y();
        trd->field_pos_x = x;
        trd->field_pos_y = y;
        trd->player = NULL;
        msg = new Message(Message::addElement, GameElement::idStation, (void *)trd);
        engine->sendMsg(msg);
        oldMousePos = e->pos();
        #warning fix me
        repaintContents(x - 45, y - 45, 90, 90, false);
        break;
      }
      case buildTrack:
      {
        struct track_data *trd;
        trd = new struct track_data;
        x = oldMousePos.x();
        y = oldMousePos.y();
        trd->field_pos_x = x;
        trd->field_pos_y = y;
        trd->player = NULL;
        msg = new Message(Message::addElement, GameElement::idTrack, (void *)trd);
        engine->sendMsg(msg);
        oldMousePos = e->pos();
        #warning fix me
        repaintContents(x - 45, y - 45, 90, 90, false);
        break;
      }
      default:
        break;
    }     
  }
}

void GameMapView::setMouseType(MouseType type)
{
  bool bRepaint = false;
  if ((type == buildTrack) || (type == buildStation))
    bRepaint = true;
  mouseType = type;
  if (bRepaint != bShowGrid)
  {
    bShowGrid = bRepaint;
    qDebug("repaint /w build ...");
    repaintContents(false);
  }
}

void GameMapView::drawContents(QPainter *p, int cx, int cy, int cw, int ch)
{
  QPixmap pixPaint;

  qDebug("zu zeichnender Bereich:  %i, %i  -  %i, %i", cx, cy, cw, ch);
  pixPaint.resize(30, 30);

  int x, y;
  int x1, y1, x2, y2;

  x1 = cx / 30;
  y1 = cy / 30;
  x2 = cw / 30;
  x2++;
  x2 += x1;
  if(x2 < engine->getWorldMap()->getWidth())
    x2++;

  y2 = ch / 30;
  y2++;
  y2 += y1;
  if(y2 < engine->getWorldMap()->getHeight())
    y2++;

  for(x=x1;x<x2;x++)
    for(y=y1;y<y2;y++)
    {
      getMapPixmap(&pixPaint, x, y);
      qDebug("zeichne bei: %i, %i", x * 30, y * 30);
      p->drawPixmap(x * 30, y * 30, pixPaint);
      if (bShowGrid)
      {
        p->setPen(Qt::darkGray);
        p->drawLine(x * 30 + 29, y * 30, x * 30 + 29, y * 30 + 29);
        p->drawLine(x * 30, y * 30 + 29, x * 30 + 29, y * 30 + 29);
      }
    }
}
