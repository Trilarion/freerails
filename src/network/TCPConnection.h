/*
 * $Id$
 */

#ifndef __TCPCONNECTION_H__
#define __TCPCONNECTION_H__

#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>

#include "Connection.h"

//extern ssize_t write(int fd,const void *buf,size_t count);

const int MAXCONNECTIONS = 5;

class TCPConnection: public Connection {

public:

    TCPConnection();
    TCPConnection(int _socketID);
    /**  */
    ~TCPConnection();
    
    int write(void* data, int len);
    int read(void* buf, int maxlen);
    
    void open(char* host, int port);
    void listen(int port);
    int accept();
    void close();

 private:
    int socketID;

};

#endif
