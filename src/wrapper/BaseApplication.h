/*
 * $Id$
 */

#ifndef __BASEAPPLICATION_H__
#define __BASEAPPLICATION_H__

class BaseApplication {

public:
    /**  */
    BaseApplication(int argc, char *argv[]);
    /**  */
    ~BaseApplication();

    virtual bool InitScreen(int x, int y, int w, int h);
    virtual void SetCaption(const char *title);
    virtual void Run();

};

#endif