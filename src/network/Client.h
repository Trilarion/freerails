/*
 * $Id$
 */

#ifndef __CLIENT_H__
#define __CLIENT_H__

#include "TCPConnection.h"

class Client: public TCPConnection {

public:

    Client();
    /**  */
    ~Client();
    
private:

    void listen(int port);
    int accept();
    
};

#endif