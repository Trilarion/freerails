/** $Id$
  * Map class (handling and viewing)
  */

#ifndef __GAMEMAP_H__
#define __GAMEMAP_H__

#include "GameMainWindow.h"
#include "GameMapView.h"
#include <qcanvas.h>

class GameMapView;
class GameMainWindow;

class GameMap : public QCanvas {
	Q_OBJECT
public:
	/** Constrcutor */
	GameMap(GameMainWindow* parent, const char* name);
	GameMap(QWidget* parent, const char* name);
	/** Destructor */
	~GameMap();
	/** Show the map */
	void Show();
	GameMapView* getWidget() { return view; };

private:
	GameMapView* view;
};

#endif // __GAMEMAP_H__
