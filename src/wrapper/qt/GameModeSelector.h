/** $Id$
  * A dialog to choose the Game Mode
  */

#ifndef __GAMEMODESELECTOR_H__
#define __GAMEMODESELECTOR_H__

#include <qdialog.h>

class GameMainWindow;
class QVBoxLayout;
class QPushButton;
class QLabel;

/** Class to select game mode
  *
  * @version $Id$
  */
class GameModeSelector : public QDialog
{
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

  protected:
    QVBoxLayout* layout;
    QPushButton* btnSingle;
    QPushButton* btnMulti;
    QPushButton* btnQuit;
    QLabel* label;
    GameMode result;

  protected slots:
    void setSingle();
    void setMulti();
    void setQuit();

};

#endif // __GAMEMODESELECTDIALOG_H__
