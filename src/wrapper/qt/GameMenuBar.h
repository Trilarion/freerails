/** $Id$
  * Vertical MenuBar class
  */
#ifndef __GAMEMENUBAR_H__
#define __GAMEMENUBAR_H__

#include <qwidget.h>
#include <qpushbutton.h>
#include <qlayout.h>


#include "GameMainWindow.h"

class GameMainWindow;

class GameMenuBar : public QWidget {
	Q_OBJECT
public:
	/** Constructor */
	GameMenuBar(QWidget* parent, const char* name);
	GameMenuBar(GameMainWindow* parent, const char* name);
	/** Destructor */
	~GameMenuBar();
	/** Show the menubar */
	void Show();

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