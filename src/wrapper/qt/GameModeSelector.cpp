/** $Id$
  * A dialog to choose the Game Mode
  */

#include <qlayout.h>
#include <qpushbutton.h>
#include <qlabel.h>
#include <qapplication.h>

#include "GameModeSelector.h"
#include "GameMainWindow.h"
#include "i18n.h"

GameModeSelector::GameModeSelector(GameMainWindow* parent)
    : QWidget(parent->getWidget()) {

  layout = new QVBoxLayout(this);

  label = new QLabel(_("Please select game mode:"), this, "label");
  butSingle = new QPushButton(_("Single player mode"), this, "single");
  butMulti = new QPushButton(_("Multiplayer mode"), this, "multi");
  butQuit = new QPushButton(_("Quit Freerails"), this, "quit");

  layout->addWidget(label);
  layout->addSpacing(20);
  layout->addWidget(butSingle);
  layout->addWidget(butMulti);
  layout->addSpacing(15);
  layout->addWidget(butQuit);

  connect(butMulti, SIGNAL(clicked()), this, SLOT(setMulti()));
  connect(butSingle, SIGNAL(clicked()), this, SLOT(setSingle()));
  connect(butQuit, SIGNAL(clicked()), this, SLOT(setQuit()));
  
  setFixedSize(sizeHint());
  // Center it
  move((parentWidget()->width() - this->width()) / 2,
      (parentWidget()->height() - this->height()) / 2 );
}

GameModeSelector::~GameModeSelector() {
// Qt deletes everything itself
  delete layout;
  delete label;
  delete butSingle;
  delete butMulti;
  delete butQuit;
}

GameModeSelector::GameMode GameModeSelector::exec() {
  show();
  qApp->enter_loop();
  return result;
}

void GameModeSelector::setMulti() {
  result = Multi;
  qApp->exit_loop();
}

void GameModeSelector::setSingle() {
  result = Single;
  qApp->exit_loop();
}

void GameModeSelector::setQuit() {
  result = Quit;
  qApp->exit_loop();
}
