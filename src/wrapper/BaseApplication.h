/*
 * $Id$
 */

#ifndef __BASEAPPLICATION_H__
#define __BASEAPPLICATION_H__

#define WRAPPERTYPE_BASE 1

class BaseApplication {

public:
    /**  */
    BaseApplication(int argc, char *argv[]);
    /**  */
    ~BaseApplication();

    virtual bool initScreen(int x, int y, int w, int h);
    virtual void setCaption(const char *title);
    virtual void run();
	virtual int wrapperType();

};

#endif