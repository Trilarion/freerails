/** $Id$
  * Map class (handling and viewing)
  */

#ifndef __GAMEMAP_H__
#define __GAMEMAP_H__

#include <qcanvas.h>
#include <qpixmap.h>

class GuiEngine;
class GameMainWindow;

class GameMap : public QCanvas
{
    Q_OBJECT
  public:
    /** Constrcutor */
    GameMap(GuiEngine *_guiEngine, GameMainWindow *parent, const char* name = 0);
    /** Destructor */
    ~GameMap();

  private:
    GuiEngine *guiEngine;

    QPixmap pixmap;
};

#endif // __GAMEMAP_H__
