/*
 * $Id$
 */

#ifndef __SERVER_H__
#define __SERVER_H__

#include "TCPConnection.h"
class Server: public TCPConnection {

public:

    Server(int port);
    /**  */
    ~Server();
    
    int getCount() {return count;};
    
private:

    void connect(char* host, int port);

    int count;
    
};

#endif
