/*
 * $Id$
 */

#ifndef __CLIENT_H__
#define __CLIENT_H__

#ifdef USE_QT
#include "QTSocket.h"

class Client: public QTSocket {
#endif

#ifdef USE_PARAGUI
#include "SDLSocket.h"

class Client: public SDLSocket {
#endif

public:

    Client();
    /**  */
    ~Client();
    
};

#endif