/*
 * $Id$
 */

#ifndef __FILECONNECTION_H__
#define __FILECONNECTION_H__

#include <unistd.h>
#include <fstream>
#include <string>

#include "Connection.h"

enum Mode { READ=0, OVERWRITE, BACKUP, APPEND};

class FileConnection: public Connection {

 public:

  FileConnection();

  /**  */
  ~FileConnection();
  
  int writeTo(void* data, int len);
  int readFrom(void* buf, int maxlen);
  
  void open(char* filename, Mode stat); 

  void close();
  
  bool endOfFile();

 private:
  fstream *file;
  Mode mode;

};

#endif
