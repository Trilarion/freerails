/*
 * $Id$
 */

#include "FileConnection.h"

FileConnection::FileConnection():Connection() {

}

FileConnection::FileConnection(int _socketID):Connection() {

  socketID=_socketID;

}

FileConnection::~FileConnection() {

  close();

}

void FileConnection::open(char* host, int port) {

}

void FileConnection::close() {

  if (state!=IDLE)
  {
    state=CLOSING;
    ::close(socketID);
  }
  state=IDLE;
}

int FileConnection::write(void* data, int len) {

  if (state==OPEN)
  {
    return 0;
  } else
  {
    error=NOT_OPEN;
    return -1;
  }
}

int FileConnection::read(void* buf, int maxlen) {

  if (state==OPEN)
  {
    return 0;
  } else
  {
    error=NOT_OPEN;
    return -1;
  }
}