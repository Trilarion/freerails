/*
 * $Id$
 */

#include "Serializer.h"
#include "Connection.h"


Serializer *Connection::serializer=new Serializer();

Connection::Connection() {

  error=NONE;
  state=IDLE;
}

Connection::~Connection() {

  close();

}

void Connection::close() {

}

void Connection::open(char* c, int i) {

}
