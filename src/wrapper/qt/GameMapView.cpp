/** $Id$
  * View a QCanvas class
  */

#include <qpainter.h>
#include <qpixmap.h>

#include "Engine.h"
#include "GameMainWindow.h"
#include "GameMapView.h"

GameMapView::GameMapView(Engine *_engine, GameMap *_map, GameMainWindow* parent, const char* name)
           : QCanvasView((QCanvas*)_map, parent->getWidget(), name)
{
  engine = _engine;
  map = _map;
  resize(parent->getWidget()->width() - 150, parent->getWidget()->height());

  bool status;
  
  pixTiles = new QPixmap();
  CHECK_PTR(pixTiles);
  status = pixTiles->load("/usr/local/share/freerails/terrain_tiles.png");
  if(!status)
    qDebug("konnte terrain_tiles.png nicht laden");
    
  pixTrack = new QPixmap();
  CHECK_PTR(pixTrack);
  status = pixTrack->load("/usr/local/share/freerails/track_tiles.png");
  if(!status)
    qDebug("konnte track_tiles.png nicht laden");

  mouseType = normal;
  mouseButton = none;
  oldMousePos.setX(0);
  oldMousePos.setY(0);
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

//  qDebug("Pos. %i, %i hat Feldtyp %i", x, y, int(type));
    
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
//  qDebug("offset in Tiles.png is %i, %i", ox, oy);
  
  bitBlt(pixPaint, 0, 0, pixTiles, ox, oy, 30, 30, Qt::CopyROP, true);
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
  qDebug("mouse pressed at: %i, %i", e->x(), e->y());

  if(e->button() == Qt::LeftButton)
  {
    mouseButton = left;
    #warning complete me
  }

  if(e->button() == Qt::RightButton)
  {
    mouseButton = right;
    #warning complete me
  }
}

void GameMapView::contentsMouseReleaseEvent(QMouseEvent *e)
{
  qDebug("mouse released at: %i, %i", e->x(), e->y());

  QPixmap pixPaint;
  QPainter *p;

  p= new QPainter();
  CHECK_PTR(p);
  
  pixPaint.resize(30, 30);

  getMapPixmap(&pixPaint, oldMousePos.x(), oldMousePos.y());
  p->begin(this);
  p->drawPixmap(oldMousePos.x() * 30, oldMousePos.y() * 30, pixPaint);
  p->end();

  delete p;

  mouseButton = none;
}

void GameMapView::contentsMouseMoveEvent(QMouseEvent *e)
{
  qDebug("mouse moved to: %i, %i", e->x(), e->y());

  if(mouseButton == right)
  {
     scrollBy(30, 30);
  }

  if(mouseButton == left)
  {
    int x, y, x1, y1, x2, y2;
    int offx, offy;
    int dir, dir2, helpx, helpy;

    offx = contentsX();
    offy = contentsY();

    offx /= 30;
    offy /= 30;
    
    x1 = e->x() - offx * 30;
    y1 = e->y() - offy * 30;

    x = x1 / 30;
    y = y1 / 30;
    
    helpx = x * 30;
    helpx = x1 - helpx;
    helpy = y * 30;
    helpy = y1 - helpy;

    if(helpx < 10)
    {
      if(helpy < 10)
      {
        dir = 8;
      }
      else
      {
        if(helpy < 20)
        {
          dir = 7;
        }
        else
        {
          dir = 6;
        }
      }
    }
    else
    {
      if(helpx < 20)
      {
        if(helpy < 10)
        {
          dir = 1;
        }
        else
        {
          if(helpy < 20)
          {
            dir = 1;
          }
          else
          {
            dir = 5;
          }
        }
      }
      else
      {
        if(helpy < 10)
        {
          dir = 2;
        }
        else
        {
          if(helpy < 20)
          {
            dir = 3;
          }
          else
          {
            dir = 4;
          }
        }
      }
    }

    if(dir < 5)
      dir2 = dir + 4;
    else
      dir2 = dir - 4;

    y2 = y;
    x2 = x;

    switch(dir)
    {
      case 1:
        y2--;
        break;
      case 2:
        y2--;
        x2++;
        break;
      case 3:
        x2++;
        break;
      case 4:
        y2++;
        x2++;
        break;
      case 5:
        y2++;
        break;
      case 6:
        y2++;
        x2--;
        break;
      case 7:
        x2--;
        break;
      case 8:
        y2--;
        x2--;
        break;
    }

    regenerateTile(x, y);

    #warning complete me
    switch(mouseType)
    {
      case buildStation:
        showTrack(x, y, offx, offy, 20 * 30 + 15, 26 * 30 + 15);
        break;
      case buildTrack:
//        if(engine->canBuildTrack(x, y, 1, dir) >= 0)
//        {
          showTrack(x, y, offx, offy, (dir - 1) * 60 + 15, 15);
//          if(engine->canBuildTrack(x2, y2, 1, dir2) >= 0)
            showTrack(x2, y2, offx, offy, (dir2 - 1) * 60 + 15, 15);
//        }
        break;
      default:
        break;
    }     
  }
}

void GameMapView::setMouseType(MouseType type)
{
  mouseType = type;
}

void GameMapView::drawContents(QPainter *p, int cx, int cy, int cw, int ch)
{
  QPixmap pixPaint;

  qDebug("zu zeichnender Bereich:  %i, %i  -  %i, %i", cx, cy, cw, ch);
  pixPaint.resize(30, 30);

  int x, y;

  for(x=0;x<30;x++)
    for(y=0;y<30;y++)
    {
      getMapPixmap(&pixPaint, x, y);
      p->drawPixmap(x * 30, y * 30, pixPaint);
    }
}

void GameMapView::regenerateTile(int x, int y)
{
  int i, ii;
  int nx, ny;

  QPixmap pixPaint;
  QPainter p(this);

  pixPaint.resize(30, 30);

  for(i=-1;i<=1;i++)
  {
    nx = oldMousePos.x() + i;
    for(ii=-1;ii<=1;ii++)
    {
      ny = oldMousePos.y() + ii;
      getMapPixmap(&pixPaint, nx, ny);
      p.drawPixmap(nx * 30, ny * 30, pixPaint);
    }
  }
  oldMousePos.setX(x);
  oldMousePos.setY(y);
}

void GameMapView::showTrack(int x, int y, int offsetX, int offsetY, int tracktileX, int tracktileY)
{
  QPixmap pixPaint;
  QPainter *p;
  qDebug("in showTrack pos: %i, %i     tile: %i, %i", x, y, tracktileX, tracktileY);

  p = new QPainter();
  CHECK_PTR(p);
  p->begin(viewport());

  pixPaint.resize(30, 30);
  qDebug("in showTrack pos: %i, %i     tile: %i, %i", x, y, tracktileX, tracktileY);
  bitBlt(&pixPaint, 0, 0, pixTrack, tracktileX, tracktileY, 30, 30, Qt::CopyROP, false);
  if(pixPaint.isNull())
    qDebug("keine Pixmap geladen");

  p->drawPixmap(x * 30, y * 30, pixPaint);
//  p->drawPixmap((x - offsetX) * 30, (y - offsetY) * 30, pixPaint);
  p->end();

  delete p;
}
