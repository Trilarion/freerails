/*
 * $Id$
 */

#include "TCPConnection.h"


/* extern int errno; */


TCPConnection::TCPConnection():Connection() {

}

TCPConnection::TCPConnection(int _socketID):Connection() {

  socketID=_socketID;
  
}

TCPConnection::~TCPConnection() {

  close();

}

void TCPConnection::open(char* host, int port) {

  state = HOSTLOOKUP;
  struct hostent* hp = gethostbyname(host);
  if (!hp)
  {
    state = ERROR;
    error = OTHER;
  }
  
  socketID = socket (AF_INET, SOCK_STREAM, 0);
  
  if (socketID <= 0)
  {
    state=ERROR;
    error=OTHER;
    return;
  } // no socket
  
  state = CONNECTING;
  
  struct in_addr in;
  memcpy(&in.s_addr, hp->h_addr_list[0], sizeof(in.s_addr));

  struct sockaddr_in m_addr;
  memset (&m_addr, 0, sizeof(m_addr));
  
  m_addr.sin_family=AF_INET;
  m_addr.sin_port = port;
  m_addr.sin_addr.s_addr = in.s_addr;
  
  int stat = ::connect( socketID, (sockaddr *) &m_addr, sizeof(m_addr));
  
  if (stat!=0)
  {
    state=ERROR;
    error=OTHER; // TODO: read problem from errno!
  } else
  {
    state=OPEN;
  }

  ::fcntl(socketID, F_SETFL, O_NONBLOCK);
  
}

void TCPConnection::listen(int port) {

  socketID = socket (AF_INET, SOCK_STREAM, 0);
  
  if (socketID <= 0)
  {
    state=ERROR;

    error=OTHER;
    return;
  } // no socket
  
  state = CONNECTING;

  struct sockaddr_in m_addr;
  memset (&m_addr, 0, sizeof(m_addr));
  
  m_addr.sin_family=AF_INET;
  m_addr.sin_port = port;
  m_addr.sin_addr.s_addr = INADDR_ANY;
  
  int stat = ::bind( socketID, (sockaddr *) &m_addr, sizeof(m_addr));
  
  if (stat!=0)
  {
    state=ERROR;
    error=OTHER; // TODO: read problem from errno!
    return;
  }
  stat = ::listen(socketID, MAXCONNECTIONS);

  if (stat!=0)
  {
    state=ERROR;
    error=OTHER; // TODO: read problem from errno!
  } else
  {
    state=LISTENING;
  }

  ::fcntl(socketID, F_SETFL, O_NONBLOCK);
}

int TCPConnection::accept() {

  struct sockaddr_in m_addr;
  socklen_t len = sizeof(m_addr);
  memset (&m_addr, 0, sizeof(m_addr));
  
/*  m_addr.sin_family=AF_INET;
  m_addr.sin_port = port;
  m_addr.sin_addr.s_addr = INADDR_ANY;
*/
  int newSockID = ::accept( socketID, (sockaddr *) &m_addr, &len);

  return newSockID;
}

void TCPConnection::close() {

  if (state!=IDLE)
  {
    state=CLOSING;
    ::close(socketID);
  }
  state=IDLE;
}

int TCPConnection::write(void* data, int len) {
  int n;

  if (state==OPEN)
  {
    if ((n=::write(socketID,data,len))<=0){
      /* error=XXXX unable to write to socket 
	 TODO: search for errors in errno */
      return -1;
    }
    return 0;
  } else
  {
    error=NOT_OPEN;
    return -1;
  }
}

int TCPConnection::read(void* buf, int maxlen) {
  int n;

  if (state==OPEN)
  {
    if((n=::read(socketID,buf,maxlen))<=0){
      /* error=XXXXX unable to read from socket 
	 TODO: search for errors in errno */
      return -1;
    }

    return 0;
  } else
  {
    error=NOT_OPEN;
    return -1;
  }
}
