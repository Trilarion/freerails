/** $Id$
  * View a QCanvas class
  */

#ifndef __GAMEMAPVIEW_H__
#define __GAMEMAPVIEW_H__

#include "GameMap.h"
#include "GameMainWindow.h"

#include <qcanvas.h>

class GameMap;

class GameMapView : public QCanvasView {
	Q_OBJECT
public:
	/** Constructor */
	GameMapView(GameMap* map, GameMainWindow* parent, const char* name);
	GameMapView(GameMap* map, QWidget* parent, const char* name);
	/** Destructor */
	~GameMapView();
	/** Event mouse pressed */
	void contentsMousePressEvent(QMouseEvent* e);
};

#endif // __GAMEMAPVIEW_H__
