/*
 * $Id$
 * Mainwindow handling class
 */

#ifndef __GAMEMAINWINDOW_H__
#define __GAMEMAINWINDOW_H__

#include "BaseMainWindow.h"
#include "GameModeSelector.h"


class QHBoxLayout;
class QVBoxLayout;
class QWidget;

class Engine;
class GameMenuBar;
class GameMap;
class GameMapView;
class GamePanel;

/** GameMainWindow class manages main window
 */
class GameMainWindow : public BaseMainWindow
{
  public:
    /** Creates new MainWindow to position @ref x, @ref y
      * Width and height are set by @ref w and @ref h
      */
    GameMainWindow(int x, int y, int w, int h);
    /** Destroys MainWindow */
    virtual ~GameMainWindow();
    /** Sets caption of MainWindow to @ref caption */
    void setCaption(const char* caption);
    /** Returns MainWindow's widget
      * For internal use only !
      */
    QWidget* getWidget() { return widget; };
    /** Displays little 'dialog' inside itself and lets user select
      * Game mode - Single player, multiplayer or exit
      */
    GameModeSelector::GameMode askGameMode();
    /** Constructs 'playfield'
      * Playfield consists of map, panel and buttons
      */
    void constructPlayField();
    void setEngine(Engine *_engine);

    void resizeEvent(QResizeEvent *);
    
  private:
    QWidget *widget;
    QVBoxLayout *layout;
    QHBoxLayout *layout_h;
    GameMenuBar *menubar;
    GameMap *map;
    GameMapView *mapview;
    GamePanel *panel;

    Engine *engine;
};

#endif
