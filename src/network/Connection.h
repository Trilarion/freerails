/*
 * $Id$
 */

#ifndef __CONNECTION_H__
#define __CONNECTION_H__

/* #include "Serializer.h" */


class Serializer;

class Connection {

public:

    enum State { IDLE=0, HOSTLOOKUP, CONNECTING, LISTENING, OPEN, CLOSING, ERROR };
    enum Error { NONE=0, NOT_OPEN, TIMEOUT, REFUSED, HOST_UNREACHABLE, NETWORK_UNREACHABLE, OTHER };

    Connection();
    /**  */
    virtual ~Connection();
    
    virtual int write(void*, int){ return 0; };
    virtual int read(void*, int){ return 0; };
    
    virtual void open(char* c, int i);
    virtual void close();

    virtual short getConnectionId(){};
    
    State getState() {return state;};
    Error getError() {return error;};
    inline Serializer *getSerializer(){return serializer;};
    
protected:

    static Serializer *serializer;

    State state;
    Error error;
    
};

#endif
