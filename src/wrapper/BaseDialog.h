/*
 * $Id$
 */

#ifndef __BASEDIALOG_H__
#define __BASEDIALOG_H__

#include "BaseWidget.h"

class BaseDialog: public BaseWidget {

public:
    /**  */
    BaseDialog();
    /**  */
    ~BaseDialog();

    int show();

};

#endif