/** $Id$
  * Map class (handling and viewing)
  */

#ifndef __GAMEMAP_H__
#define __GAMEMAP_H__

#include <qcanvas.h>
#include <qpixmap.h>

class Engine;
class GameMainWindow;

class GameMap : public QCanvas
{
    Q_OBJECT
  public:
    /** Constrcutor */
    GameMap(Engine *_engine, GameMainWindow *parent, const char* name = 0);
    /** Destructor */
    ~GameMap();

  private:
    Engine *engine;

    QPixmap pixmap;
};

#endif // __GAMEMAP_H__
