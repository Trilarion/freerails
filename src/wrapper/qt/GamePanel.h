/** $Id$
  * Panel (minimap, train info, ...) class
  */

#ifndef __GAMEPANEL_H__
#define __GAMEPANEL_H__

#include <qwidget.h>

class Engine;
class GameMainWindow;
class GameMapView;

class QToolButton;

class GamePanel : public QWidget
{
    Q_OBJECT
  public:
    /** Constructor */
    GamePanel(Engine *_engine, GameMapView *_mapView, GameMainWindow* parent, const char* name = 0);
    /** Destructor */
    ~GamePanel();
    
  private:
    void releaseAllButtons();
    void setupButtons();
    void showPanel();

    QToolButton *btnTrack;
    QToolButton *btnStation;
    QToolButton *btnPause;
    QToolButton *btnExit;

    Engine *engine;
    GameMapView *mapView;

  private slots:
    void handler_pause();
    void handler_track();
    void handler_station();
    void handler_exit();
};


#endif // __GAMEPANEL_H__
