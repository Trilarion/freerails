/** $Id$
  * Panel (minimap, train info, ...) class
  */

#ifndef __GAMEPANEL_H__
#define __GAMEPANEL_H__

#include <qwidget.h>

class GuiEngine;
class GameMainWindow;
class GameMapView;

class QToolButton;
class QWidgetStack;

class GamePanel : public QWidget
{
    Q_OBJECT
  public:
    /** Constructor */
    GamePanel(GuiEngine *_guiEngine, GameMapView *_mapView, GameMainWindow* parent, const char* name = 0);
    /** Destructor */
    ~GamePanel();
    
  private:
    void releaseAllButtons();
    void setupWdgMessages();
    void setupButtons();
    void setupWstTrainBuild();
    void setupWdgMiniMap();

    void setupPanelTrain();
    void setupPanelBuild();
    
    void showPanel();

    QToolButton *btnTabTrain;
    QToolButton *btnTabBuild;
    
    QToolButton *btnTrack;
    QToolButton *btnStation;
    QToolButton *btnPause;
    QToolButton *btnExit;

    QWidget *wdgMiniMap;
    QWidget *wdgMessages;
    QWidget *wdgTrain;
    QWidget *wdgBuild;
    QWidget *panelTrain;
    QWidget *panelBuild;
    QWidgetStack *wstTrainBuild;

    GuiEngine *guiEngine;
    GameMapView *mapView;
    GameMainWindow *mainWindow;

    bool build_is_on;

  private slots:
    void slotTabTrain();
    void slotTabBuild();
    
    void handler_pause();
    void handler_track();
    void handler_station();
    void handler_exit();

};


#endif // __GAMEPANEL_H__
