/** $Id$
  * View a QCanvas class
  */

#ifndef __GAMEMAPVIEW_H__
#define __GAMEMAPVIEW_H__

#include "MapField.h"
#include "Base2DMapView.h"

#include <qcanvas.h>
#include <qpoint.h>

class QPainter;
class QPixmap;

class GuiEngine;
class GameMainWindow;
class GameMap;

typedef struct field_data
{
  int field_x;          // offset for picture of field
  int field_y;
  int track_x[5];       // offset for picture of track and edges
  int track_y[5];
};

class GameMapView : public QCanvasView, Base2DMapView
{
    Q_OBJECT

  public:
    enum MouseType {normal = 0, 
                    buildTrack = 10, buildStation, buildSignal};
    enum MouseButton {none = 0, left, middle, right};
    
    /** Constructor */
    GameMapView(GuiEngine *_guiEngine, GameMap *_map, GameMainWindow  *parent, const char *name = 0);
    /** Destructor */
    ~GameMapView();
    /** Event mouse pressed */
    void setMouseType(MouseType type);
    void drawContents(QPainter *p, int cx, int cy, int cw, int ch);

  protected:
    void contentsMousePressEvent(QMouseEvent *e);
    void contentsMouseReleaseEvent(QMouseEvent *e);
    void contentsMouseMoveEvent(QMouseEvent *e);
    
  private:
    void updatePixmapPos(int x, int y);
    void getMapPixmap(QPixmap *pixPaint, int x, int y);

    struct field_data *fldData;
    
    MouseType mouseType;
    GuiEngine *guiEngine;
    GameMap *map;
    QPixmap *pixTiles;
    QPixmap *pixTrack;

    MouseButton mouseButton;

    QPoint oldMousePos;
    QPoint oldMousePos2;

    bool bShowGrid;
};

#endif // __GAMEMAPVIEW_H__
