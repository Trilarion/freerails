/** $Id$
  * A dialog to choose the Game Mode
  */

#ifndef __GAMEMODESELECTOR_H__
#define __GAMEMODESELECTOR_H__

#include <qwidget.h>

class GameMainWindow;
class QVBoxLayout;
class QPushButton;
class QLabel;

/** Class to select game mode
  *
  * @version $Id$
  */
class GameModeSelector : public QWidget {
  Q_OBJECT
public:
  /** Game modes */
  enum GameMode { Quit = 0, Single, Multi };
  /** Constructor */
  GameModeSelector(GameMainWindow* parent);
  /** Destructor */
  ~GameModeSelector();
  /** Executes the dialog
    * This enters own event loop, like QDialog, so you don't need to call
    * QApplication::exec() before
    * Returns the selected mode
    */
  GameMode exec();

private:
  QVBoxLayout* layout;
  QPushButton* butSingle;
  QPushButton* butMulti;
  QPushButton* butQuit;
  QLabel* label;
  GameMode result;

private slots:
  void setSingle();
  void setMulti();
  void setQuit();

};

#endif // __GAMEMODESELECTDIALOG_H__
