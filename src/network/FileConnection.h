/*
 * $Id$
 */

#ifndef __FILECONNECTION_H__
#define __FILECONNECTION_H__

#include <unistd.h>

#include "Connection.h"

class FileConnection: public Connection {

public:

    FileConnection();
    FileConnection(int _socketID);
    /**  */
    ~FileConnection();
    
    int write(void* data, int len);
    int read(void* buf, int maxlen);
    
    void open(char* filename, int stat);
    void close();

};

#endif