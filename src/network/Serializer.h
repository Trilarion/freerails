/** $Id$
  */
 
#ifndef __SERIALIZER_H__
#define __SERIALIZER_H__

#include "Connection.h"
#include <unistd.h>
#include <string>

class Serializer {
public:

  virtual ~Serializer();

  void setConnection(Connection* con) { myConnection=con;};
  Connection* getConnection() { return myConnection;};
    
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

  size_t write_long          (long l);
  size_t write_long_unsigned (long unsigned l);
  size_t write_int           (int i);
  size_t write_int_unsigned  (int unsigned i);
  size_t write_short         (short s);
  size_t write_char          (char c);
  size_t write_string        (const std::string &s);

  size_t read_long           (long& l);
  size_t read_long_unsigned  (long unsigned& l);
  size_t read_int            (int& i);
  size_t read_int_unsigned   (int unsigned& i);
  size_t read_short          (short& s);
  size_t read_char           (char& c);
  size_t read_string         (std::string& s);
  
private:
  Connection *myConnection;
  
};

#endif // __SERIALIZER_H__
