/*
 * $Id$
 */

#include "Connection.h"

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

