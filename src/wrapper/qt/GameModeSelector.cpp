/** $Id$
  * A dialog to choose the Game Mode
  */

#include <qapplication.h>
#include <qlabel.h>
#include <qlayout.h>
#include <qpixmap.h>
#include <qpushbutton.h>

#include "GameModeSelector.h"
#include "GameMainWindow.h"
#include "i18n.h"

GameModeSelector::GameModeSelector(GameMainWindow* parent)
    : QDialog(parent->getWidget(), 0, true,
      Qt::WStyle_Customize | Qt::WStyle_NoBorder)
{
  setBackgroundMode(Qt::FixedPixmap);
  QPixmap pixBackground("data/graphics/ui/menu_background.png");
  setPaletteBackgroundPixmap(pixBackground);
  setFixedSize(pixBackground.size());
  layout = new QVBoxLayout(this);

  label = new QLabel(this);
  label->setPaletteForegroundColor(QColor("red"));
  label->setBackgroundMode(Qt::FixedPixmap, Qt::FixedPixmap);
  label->setText(_("Please select game mode"));
  
  btnSingle = new QPushButton(_("Single player mode"),this);
  btnMulti = new QPushButton(_("Multiplayer mode"),this);
  btnQuit = new QPushButton(_("Quit Freerails"),this);

  layout->addStretch(1);
  QHBoxLayout *layout_h = new QHBoxLayout();
  layout_h->addStretch(1);
  layout_h->addWidget(label);
  layout_h->addStretch(1);
  layout->addLayout(layout_h);
  layout->addSpacing(20);
  
  layout_h = new QHBoxLayout();
  layout_h->addStretch(1);
  layout_h->addWidget(btnSingle);
  layout_h->addStretch(1);
  layout->addLayout(layout_h);
  layout->addSpacing(10);

  layout_h = new QHBoxLayout();
  layout_h->addStretch(1);
  layout_h->addWidget(btnMulti);
  layout_h->addStretch(1);
  layout->addLayout(layout_h);
  layout->addSpacing(15);

  layout_h = new QHBoxLayout();
  layout_h->addStretch(1);
  layout_h->addWidget(btnQuit);
  layout_h->addStretch(1);
  layout->addLayout(layout_h);
  layout->addStretch(1);
  layout->activate();
  
  connect(btnMulti, SIGNAL(clicked()), this, SLOT(setMulti()));
  connect(btnSingle, SIGNAL(clicked()), this, SLOT(setSingle()));
  connect(btnQuit, SIGNAL(clicked()), this, SLOT(setQuit()));
  
  // redo background for label and buttons
  QPixmap pix_tmp;

  pix_tmp.resize(label->size());
  bitBlt(&pix_tmp, 0, 0, &pixBackground,
    label->pos().x(), label->pos().y(),
    label->size().width(), label->size().height());
  label->setPaletteBackgroundPixmap(pix_tmp);

  // Center it
  move((parentWidget()->width() - this->width()) / 2,
      (parentWidget()->height() - this->height()) / 2 );
}

GameModeSelector::~GameModeSelector()
{
}

void GameModeSelector::setMulti()
{
  done(Multi);
}

void GameModeSelector::setSingle()
{
  done(Single);
}

void GameModeSelector::setQuit()
{
  done(Quit);
}
