/***************************************************************************
                          singlegameoptiondialog.h  -  description
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

#ifndef SINGLEGAMEOPTIONDIALOG_H
#define SINGLEGAMEOPTIONDIALOG_H

#include <qdialog.h>
#include <qlineedit.h>
#include <qstring.h>

class GameMainWindow;

class QBoxLayout;
class QLabel;
class QPushButton;
class QWidget;

/**
  *@author frank
  */

class SingleGameOptionDialog : public QDialog
{
   Q_OBJECT
  public:
    SingleGameOptionDialog(GameMainWindow *parent = 0);
    ~SingleGameOptionDialog();


  protected:
    QLabel *lblName;
    QLabel *lblWidth;
    QLabel *lblHeight;
    
    QLineEdit *ledName;
    QLineEdit *ledWidth;
    QLineEdit *ledHeight;

    QPushButton *btnOk;
    QPushButton *btnCancel;

    QBoxLayout *boxlayout;

  protected slots:
    void saveValues();
    
  private:
    void initDialog();
    void setupLayout();

  public:
    QString getName() { return ledName->text(); };
    int getWidth() {return ledWidth->text().toInt(); };
    int getHeight() { return ledHeight->text().toInt(); };
};

#endif
