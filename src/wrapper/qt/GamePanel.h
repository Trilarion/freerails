/** $Id$
  * Panel (minimap, train info, ...) class
  */

#ifndef __GAMEPANEL_H__
#define __GAMEPANEL_H__

#include "GameMainWindow.h"

#include <qframe.h>
#include <qpushbutton.h>

class GameMainWindow;

class GamePanel : public QWidget {
  Q_OBJECT
public:
  /** Constructor */
  GamePanel(QWidget* parent, const char* name);
  GamePanel(GameMainWindow* parent, const char* name);
  /** Destructor */
  ~GamePanel();
private:
  QPushButton* but; //TEMP
};


#endif // __GAMEPANEL_H__
