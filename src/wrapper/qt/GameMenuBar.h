/** $Id$
  * Vertical MenuBar class
  */

#ifndef __GAMEMENUBAR_H__
#define __GAMEMENUBAR_H__

#include <qwidget.h>

#include "GameMainWindow.h"

class GameMainWindow;
class QPushButton;
class QVBoxLayout;

/** @short Widget, that contains buttons to build trains, tracks etc.
  * GameMenuBar has buttons to control the game and construct new things:
  * stations, trains, railroads. It has same buttons as in RT2
  *
  * @version $Id$
  */
class GameMenuBar : public QWidget {
  Q_OBJECT
public:
  /** Constructor */
  // Do we need constructor with QWidget (we only construct it in MainWin anyway)
  GameMenuBar(QWidget* parent, const char* name);
  GameMenuBar(GameMainWindow* parent, const char* name);
  /** Destructor */
  ~GameMenuBar();
  /** Show the menubar */
  void show();

private:
  void init();
  bool initDone;


  QVBoxLayout* layout;
  QPushButton* bBuildRail;
  QPushButton* bBuildStation;
  QPushButton* bBulldoze;
  QPushButton* bMapOverview;
  QPushButton* bPurchaseTrain;
  QPushButton* bStockMarket;
  QPushButton* bControllPanel;
  QPushButton* bOptions;
};

#endif // __GAMEMENUBAR_H__

