#ifndef __BUFFER_H__
#define __BUFFER_H__

/* #include <sys/uio.h> */
#include "Network.h"
#include <stdlib.h>

#define BUFFERS_MAX_SIZE 30



class NetBuffer{
 public:

  enum BufferPos {
    ID,
    SIZE,
    FILLED,
    READ,
    BUFFER
  };

  NetBuffer();
  ~NetBuffer();
  
  void written(size_t size);
  void *writeToBuffer();
  short canWriteToBuffer(short _id);
  int sizeToWrite(short _id);
  short readNextBuffer();
  void read(void *x, int size);

  short isThereAnyData(){return buffersInUse;};

 private:

  /* buffers[i][ID] == CONNECTION_ID
     buffers[i][SIZE] == Buffer Should Contain n BYTES 
     buffers[i][FILLED] == Buffer Contains n BYTES
     buffers[i][READ] == Bytes READ
     buffers[i][BUFFER] == BUFFER */
  void *buffer[BUFFERS_MAX_SIZE][5];

  short start,end,writeBuffer,readBuffer;
  short buffersInUse; /* variable to register how many buffers are in use */

  
  /* we shall see .... */
  /*
  struct iovec buffers[12];
  int bufferNum;
  */
  
  void cleanBuffer(short _id);
  short findLastBuffer(short _id);
  void incStart();
  void incEnd();
  void incVar(short &_id);
};




#endif /* __BUFFER_H__ */
