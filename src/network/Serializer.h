/** $Id$
  */
 
#ifndef __SERIALIZER_H__
#define __SERIALIZER_H__

#include "Connection.h"
#include "Network.h"
#include "NetBuffer.h"

#include <unistd.h>
#include <string>



class Serializer {
public:

  Serializer();
  virtual ~Serializer();

  /*
  void setConnection(Connection* con) { myConnection=con;};
  Connection* getConnection() { return myConnection;};
  */

  void initSend(MSG_TYPE msg);
  size_t finishSend(Connection *_myConnection);
  short receive(Connection *_myConnection);

  const Serializer& operator << (long l);
  const Serializer& operator << (long unsigned l);
  const Serializer& operator << (int i);
  const Serializer& operator << (int unsigned i);
  const Serializer& operator << (short s);
  const Serializer& operator << (char c);
  const Serializer& operator << (const std::string &s);
  
  const Serializer& operator >> (long& l);
  const Serializer& operator >> (long unsigned& l);
  const Serializer& operator >> (int& i);
  const Serializer& operator >> (int unsigned& i);
  const Serializer& operator >> (short& s);
  const Serializer& operator >> (char& c);
  const Serializer& operator >> (std::string &s);
  


protected:

  void write_long          (long l);
  void write_long_unsigned (long unsigned l);
  void write_int           (int i);
  void write_int_unsigned  (int unsigned i);
  void write_short         (short s);
  void write_char          (char c);
  void write_string        (const std::string &s);

  void read_long           (long& l);
  void read_long_unsigned  (long unsigned& l);
  void read_int            (int& i);
  void read_int_unsigned   (int unsigned& i);
  void read_short          (short& s);
  void read_char           (char& c);
  void read_string         (std::string& s);
  
private:
  /*  Connection *myConnection; */
  
  void *write_buffer;
  /* void *read_buffer; */
  /*int read_counter, write_read_counter; */
  int write_counter;
  
  NetBuffer *readBuffer;
  
  /* implements a buffer */
  /* void *buffer[10]; */
  

  inline bool enoughSpace(short s);
  inline bool enoughData(short s);
};

#endif // __SERIALIZER_H__
