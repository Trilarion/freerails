/** $Id$
  * View a QCanvas class
  */

#ifndef __GAMEMAPVIEW_H__
#define __GAMEMAPVIEW_H__

#include "MapField.h"

#include <qcanvas.h>
#include <qpoint.h>

class QPainter;
class QPixmap;

class Engine;
class GameMainWindow;
class GameMap;

class GameMapView : public QCanvasView
{
    Q_OBJECT

  public:
    enum MouseType {normal = 0, 
                    buildTrack = 10, buildStation, buildSignal};
    enum MouseButton {none = 0, left, middle, right};
    
    /** Constructor */
    GameMapView(Engine *_engine, GameMap *_map, GameMainWindow  *parent, const char *name = 0);
    /** Destructor */
    ~GameMapView();
    /** Event mouse pressed */
    void setMouseType(MouseType type);
    void drawContents(QPainter *p, int cx, int cy, int cw, int ch);

  protected:
    void contentsMousePressEvent(QMouseEvent *e);
    void contentsMouseReleaseEvent(QMouseEvent *);
    void contentsMouseMoveEvent(QMouseEvent *e);
    
  private:
    void getMapPixmap(QPixmap *pixPaint, int x, int y);
    int getPixmapPos(int x, int y, MapField::FieldType type);
    int getRiverPixmapPos(int x, int y);
    int get3DPixmapPos(int x, int y, MapField::FieldType type);

    MouseType mouseType;
    Engine *engine;
    GameMap *map;
    QPixmap *pixTiles;
    QPixmap *pixTrack;

    MouseButton mouseButton;

    QPoint oldMousePos;
    QPoint oldMousePos2;

    bool bShowGrid;
};

#endif // __GAMEMAPVIEW_H__
