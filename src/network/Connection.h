/*
 * $Id$
 */

#ifndef __CONNECTION_H__
#define __CONNECTION_H__

class Connection {

public:

    enum State { IDLE=0, HOSTLOOKUP, CONNECTING, LISTENING, OPEN, CLOSING, ERROR };
    enum Error { NONE=0, NOT_OPEN, TIMEOUT, REFUSED, HOST_UNREACHABLE, NETWORK_UNREACHABLE, OTHER };

    Connection();
    /**  */
    ~Connection();
    
    virtual int writeTo(void* data, int len){};
    virtual int readFrom(void* buf, int maxlen){};
    
    virtual void open(char* c, int i);
    virtual void close();
    
    State getState() {return state;};
    Error getError() {return error;};
    
protected:

    State state;
    Error error;
    
};

#endif
