/** $Id$
  * View a QCanvas class
  */

#include <qpainter.h>
#include <qpixmap.h>

#include "GuiEngine.h"
#include "GameMainWindow.h"
#include "GameMapView.h"
#include "GameWidget.h"
#include "Message.h"
#include "StationController.h"
#include "Track.h"
#include "Station.h"
#include "TrackController.h"

GameMapView::GameMapView(GuiEngine *_guiEngine, GameMap *_map, GameMainWindow* parent, const char* name)
           : QCanvasView((QCanvas*)_map, (QWidget*)parent->getWidget(), name,
               Qt::WStyle_Customize | Qt::WStyle_NoBorder)
{
  guiEngine = _guiEngine;
  map = _map;
  setFrameShape(QFrame::NoFrame);
  setBackgroundMode(Qt::NoBackground);
  setVScrollBarMode(QScrollView::AlwaysOff);
  setHScrollBarMode(QScrollView::AlwaysOff);
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

  int i1, i2, w, h;
  w = guiEngine->getWorldMap()->getWidth();
  h = guiEngine->getWorldMap()->getHeight();
  fldData = new field_data[h * w];
  CHECK_PTR(fldData);

  for(i1=0;i1<h;i1++)
    for(i2=0;i2<w;i2++)
      updatePixmapPos(i2, i1);
}

GameMapView::~GameMapView()
{
  delete fldData;
}

void GameMapView::getMapPixmap(QPixmap *pixPaint, int x, int y)
{
  int element_offset;
  int ox, oy, i;

  element_offset = y * guiEngine->getWorldMap()->getWidth() + x;

  ox = fldData[element_offset].field_x;
  oy = fldData[element_offset].field_y;
  bitBlt(pixPaint, 0, 0, pixTiles, ox, oy, 30, 30, Qt::CopyROP, true);

  for(i=0;i<5;i++)
  {
    ox = fldData[element_offset].track_x[i];
    oy = fldData[element_offset].track_y[i];
    if (ox >= 0)
      bitBlt(pixPaint, 0, 0, pixTrack, ox, oy, 30, 30, Qt::CopyROP, false);
  }
}

void GameMapView::updatePixmapPos(int x, int y)
{
  MapField *field;

  field = guiEngine->getWorldMap()->getMapField(x, y);
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
    case MapField::village:
      ox = 3 * 30;
      oy = 7 * 30;
      break;
    case MapField::farm:
      ox = 6 * 30;
      oy = 7 * 30;
      break;
    case MapField::industrie:
      ox = 5 * 30;
      oy = 7 * 30;
      break;
    case MapField::resource:
      ox = 10 * 30;
      oy = 7 * 30;
      break;
    default:
      ox = 13 * 30;
      oy = 7 * 30;
      break;
  }
  
  int element_offset, i;

  element_offset = y * guiEngine->getWorldMap()->getWidth() + x;
  fldData[element_offset].field_x = ox;
  fldData[element_offset].field_y = oy;

//  Track *trk;
//  trk = field->getTrack();
//  if (trk != NULL)
//  {
    for(i=0;i<5;i++)
    {
//      trk->getTrackTile(i, &tracktileX, &tracktileY);
//      if (tracktileX >= 0)
//        bitBlt(pixPaint, 0, 0, pixTrack, tracktileX, tracktileY, 30, 30, Qt::CopyROP, false);
      fldData[element_offset].track_x[i] = -1;
      fldData[element_offset].track_x[i] = -1;
    }
//  }
}

void GameMapView::contentsMousePressEvent(QMouseEvent* e)
{
  if(e->button() == Qt::LeftButton)
  {
    mouseButton = left;
    oldMousePos = e->pos();
    #warning complete me
  }

  if(e->button() == Qt::RightButton)
  {
    mouseButton = right;
    oldMousePos2 = e->pos();
  }
}

void GameMapView::contentsMouseReleaseEvent(QMouseEvent *e)
{

  qDebug("in MouseReleaseEvent");
  if(mouseButton == left)
  {
    Message *msg;
    int x, y;

    x = oldMousePos.x();
    y = oldMousePos.y();

    qDebug("old (%d,%d)    new (%d,%d)", x, y, e->x(), e->y());
    if ((x == e->x()) && (y == e->y()))
    {
      #warning complete me
      switch(mouseType)
      {
        case buildStation:
        {
	  /*
	  Station* new_station = new Station(x, y, NULL, "", Station::Small, "", NULL);
          msg = new Message(Message::addElement, 0, (void *)new_station);
          guiEngine->sendMsg(msg);
	  */
	  guiEngine->buildStation(x,y);

          break;
        }
        case buildTrack:
        {
	  /*
          Track* new_track = new Track(x,y,NULL,0);
          msg = new Message(Message::addElement, 0, (void *)new_track);
          guiEngine->sendMsg(msg);
	  */
	  guiEngine->buildTrack(x,y,0);
          break;
        }
        default:
          break;
      }
    }
  }
  mouseButton = none;
}

void GameMapView::contentsMouseMoveEvent(QMouseEvent *e)
{
  if(mouseButton == right)
  {
    QPoint diff = e->pos() - oldMousePos2;
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
        #warning Do not build on mouse move
/*
        struct station_data *trd;
        trd = new struct station_data;
        x = oldMousePos.x();
        y = oldMousePos.y();
        trd->field_pos_x = x;
        trd->field_pos_y = y;
        trd->player = NULL;
        msg = new Message(Message::addElement, 0, (void *)trd);
        engine->sendMsg(msg);
        oldMousePos = e->pos();
        #warning fix me
        repaintContents(x - 45, y - 45, 90, 90, false);
        break;
*/
      }
      case buildTrack:
      {
/*
        struct track_data *trd;
        trd = new struct track_data;
        x = oldMousePos.x();
        y = oldMousePos.y();
        trd->field_pos_x = x;
        trd->field_pos_y = y;
        trd->player = NULL;
        msg = new Message(Message::addElement, 0, (void *)trd);
        engine->sendMsg(msg);
        oldMousePos = e->pos();
        #warning fix me
        repaintContents(x - 45, y - 45, 90, 90, false);
        break;
*/
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
    repaintContents(false);
  }
}

void GameMapView::drawContents(QPainter *p, int cx, int cy, int cw, int ch)
{
  QPixmap pixPaint;

//  qDebug("zu zeichnender Bereich:  %i, %i  -  %i, %i", cx, cy, cw, ch);
  pixPaint.resize(30, 30);

  int x, y;
  int x1, y1, x2, y2;

  x1 = cx / 30;
  y1 = cy / 30;
  x2 = cw / 30;
  x2++;
  x2 += x1;
  if(x2 < guiEngine->getWorldMap()->getWidth())
    x2++;

  y2 = ch / 30;
  y2++;
  y2 += y1;
  if(y2 < guiEngine->getWorldMap()->getHeight())
    y2++;

  for(x=x1;x<x2;x++)
    for(y=y1;y<y2;y++)
    {
      getMapPixmap(&pixPaint, x, y);
//      qDebug("zeichne bei: %i, %i", x * 30, y * 30);
      p->drawPixmap(x * 30, y * 30, pixPaint);
      if (bShowGrid)
      {
        p->setPen(Qt::darkGray);
        p->drawLine(x * 30 + 29, y * 30, x * 30 + 29, y * 30 + 29);
        p->drawLine(x * 30, y * 30 + 29, x * 30 + 29, y * 30 + 29);
      }
    }
}
