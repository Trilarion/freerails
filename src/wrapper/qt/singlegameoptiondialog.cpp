/***************************************************************************
                          singlegameoptiondialog.cpp  -  description
                             -------------------
    begin                : Mit Sep 18 2002
    copyright            : (C) 2002 by Frank Schmischke
    email                : frank.schmischke@t-online.de
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
#include <qlabel.h>
#include <qlayout.h>
#include <qpushbutton.h>
#include <qwidget.h>

#include "GameMainWindow.h"
#include "i18n.h"
#include "singlegameoptiondialog.h"

SingleGameOptionDialog::SingleGameOptionDialog(GameMainWindow *parent)
  : QDialog(parent->getWidget(), 0, true,
      Qt::WStyle_Customize | Qt::WStyle_NoBorder)
{
  initDialog();
  setupLayout();

  connect(btnOk, SIGNAL(clicked()), SLOT(saveValues()));
  connect(btnCancel, SIGNAL(clicked()), SLOT(reject()));
}

SingleGameOptionDialog::~SingleGameOptionDialog()
{
}

void SingleGameOptionDialog::initDialog()
{
  lblName = new QLabel(_("your name"), this);
  CHECK_PTR(lblName);

  lblWidth = new QLabel(_("with of gameboard"), this);
  CHECK_PTR(lblWidth);

  lblHeight = new QLabel(_("height of gameboard"), this);
  CHECK_PTR(lblHeight);

  ledName = new QLineEdit("Lukas", this);
  CHECK_PTR(ledName);

  ledWidth = new QLineEdit("30", this);
  CHECK_PTR(ledWidth);

  ledHeight = new QLineEdit("30", this);
  CHECK_PTR(ledHeight);
  
  btnOk = new QPushButton(this);
  CHECK_PTR(btnOk);
  btnOk->setText(_("&OK"));
  btnOk->setMinimumSize(btnOk->sizeHint());

  btnCancel = new QPushButton(this);
  CHECK_PTR(btnCancel);
  btnCancel->setText(_("&Cancel"));
  btnCancel->setMinimumSize(btnCancel->sizeHint());
}

void SingleGameOptionDialog::setupLayout()
{
  boxlayout = new QBoxLayout(this, QBoxLayout::Down);
  CHECK_PTR(boxlayout);
  boxlayout->addSpacing(10);

  QBoxLayout *tmp = new QBoxLayout(QBoxLayout::LeftToRight);
  CHECK_PTR(tmp);
  boxlayout->addLayout(tmp);
  tmp->addSpacing(5);
  tmp->addWidget(lblName);
  tmp->addSpacing(5);
  tmp->addWidget(ledName);
  tmp->addSpacing(5);
  tmp->addStretch(1);
  boxlayout->addSpacing(10);

  tmp = new QBoxLayout(QBoxLayout::LeftToRight);
  CHECK_PTR(tmp);
  boxlayout->addLayout(tmp);
  tmp->addSpacing(5);
  tmp->addWidget(lblWidth);
  tmp->addSpacing(5);
  tmp->addWidget(ledWidth);
  tmp->addSpacing(5);
  tmp->addStretch(1);
  boxlayout->addSpacing(10);

  tmp = new QBoxLayout(QBoxLayout::LeftToRight);
  CHECK_PTR(tmp);
  boxlayout->addLayout(tmp);
  tmp->addSpacing(5);
  tmp->addWidget(lblHeight);
  tmp->addSpacing(5);
  tmp->addWidget(ledHeight);
  tmp->addSpacing(5);
  tmp->addStretch(1);
  boxlayout->addSpacing(10);

  tmp = new QBoxLayout(QBoxLayout::LeftToRight);
  CHECK_PTR(tmp);
  boxlayout->addLayout(tmp);
  tmp->addSpacing(10);
  tmp->addWidget(btnOk);
  tmp->addSpacing(10);
  tmp->addStretch(1);
  tmp->addWidget(btnCancel);
  tmp->addSpacing(10);
  boxlayout->addSpacing(10);

  boxlayout->activate();
  this->resize(0,0);
  boxlayout->freeze();
}

void SingleGameOptionDialog::saveValues()
{
  bool status;
  
  if(ledName->text().isEmpty())
  {
    ledName->setFocus();
    return;
  }

  if(ledWidth->text().isEmpty())
  {
    ledWidth->setFocus();
    return;
  }
  ledWidth->text().toUInt(&status);
  if(!status)
  {
    ledWidth->setFocus();
    return;
  }

  if(ledHeight->text().isEmpty())
  {
    ledHeight->setFocus();
    return;
  }
  ledHeight->text().toUInt(&status);
  if(!status)
  {
    ledHeight->setFocus();
    return;
  }

  emit accept();
}
