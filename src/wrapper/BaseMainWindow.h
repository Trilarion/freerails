/*
 * $Id$
 * Class to create main window of game
 */

#ifndef __BASEMAINWINDOW_H__
#define __BASEMAINWINDOW_H__

/** Class, that should construct main window of game
  */
class BaseMainWindow
{
  public:
    /** Should create main window to position @ref x, @ref y,
      * with size @ref w X @ref h
      */
    BaseMainWindow(int x, int y, int w, int h);
    /** Should destroy (and hide) mainwindow */
    ~BaseMainWindow();
    /** Should set mainwindow's caption to @ref caption */
    virtual void setCaption(const char* caption) = 0;
};

#endif
