#include "NetBuffer.h"

#include "FreeRailsLog.h"
#include <string.h>

NetBuffer::NetBuffer(){
  int i;

  for(i=0;i<BUFFERS_MAX_SIZE;i++){
    buffer[i][ID]=malloc(sizeof(short));
    buffer[i][ID]=(void *)0;
    buffer[i][SIZE]=malloc(sizeof(int));
    buffer[i][SIZE]=(void *)0;
    buffer[i][FILLED]=malloc(sizeof(int));
    buffer[i][FILLED]=(void *)0;
    buffer[i][READ]=malloc(sizeof(int));
    buffer[i][READ]=(void *)0;
    buffer[i][BUFFER]=malloc(sizeof(char)*MAX_MSG_SIZE+1);
    memset(buffer[i][BUFFER],'\0',MAX_MSG_SIZE+1);
  }

  FreeRailsLog("NetBuffer: CREATING BUFFER");

  start=0;
  end=0;
  writeBuffer=readBuffer=0;
  buffersInUse=0;
  
}


NetBuffer::~NetBuffer(){

  int i;
  for(i=0;i<BUFFERS_MAX_SIZE;i++){
    free(buffer[i][0]);
    free(buffer[i][1]);
  }
  
}


void NetBuffer::written(size_t _size){

  *(int *)buffer[writeBuffer][FILLED]+=_size;

  if( (*(int *)buffer[writeBuffer][SIZE] == 0) && 
      (*(int *)buffer[writeBuffer][FILLED] >1 )) {
    /* if we don't know the size of the packet, and there is enough
       bytes, than we'll find the packet size :) */
    read(&buffer[writeBuffer][SIZE],sizeof(int));
  }
  
}
      
      

short NetBuffer::findLastBuffer(short _id){
  
  int pointer=end, last=-1;
  
  if( end == start)
    return -1;

  do{
    
    if( _id == (short)buffer[pointer][ID]){ 
      if( (int)buffer[pointer][SIZE] == (int)buffer[pointer][FILLED] )
	return -1; 
      else
	return pointer;
    }

    if( ++pointer == BUFFERS_MAX_SIZE)
      pointer=0;

  }while( pointer != start );

  return -1; /* there is no buffer with this _id */

}

short NetBuffer::canWriteToBuffer(short _id){
  
  short buff;
  
  if( (buff=findLastBuffer(_id)) == -1){
    if( start != end){
      writeBuffer=end++;
      buffersInUse++;
      return writeBuffer;
    }
    else
      return -1; /* Don't Have Any Free NetBuffers */
  }else{
    writeBuffer=buff;
    return buff; 
  }
  
  return -1; /* Don't Have Any Free NetBuffers */
}

void * NetBuffer::writeToBuffer(){
  
  return &(buffer[writeBuffer][BUFFER]);

}

void NetBuffer::incStart(){

  if( start == end){
    FreeRailsLog("NetBuffer: cannot increment start (start==end)");
    return;
  }

  do{
    if( ++start == BUFFERS_MAX_SIZE)
      start=0;
  }while( buffer[start][ID] == 0 );
 
}

void NetBuffer::incEnd(){

  if(++end == BUFFERS_MAX_SIZE)
    start=0;
  else if( start == end){
    FreeRailsLog("NetBuffer: cannot increment end (end == --start )");
    end--;
    return;
  }
    
}


void NetBuffer::cleanBuffer(short _id){

  buffer[_id][ID]=(void *)0;
  buffer[_id][SIZE]=(void *)0;
  buffer[_id][FILLED]=(void *)0;
  buffer[_id][READ]=(void *)0;
  memset(buffer[_id][BUFFER],'\0',MAX_MSG_SIZE+1);
  buffersInUse--;

}


void NetBuffer::read(void *x, int size){
  memcpy(x, (char *)buffer[readBuffer][BUFFER] + *(int *)buffer[readBuffer][READ], size);
  *(int *)buffer[readBuffer][READ]+=size;
  
  /* lets try to get RI(EA)D of this buffer :) */
  if(*(int *)buffer[readBuffer][READ] == *(int *)buffer[readBuffer][SIZE]){
    if(start == readBuffer)
      incStart();
    cleanBuffer(readBuffer);
  }
    
}


int NetBuffer::sizeToWrite(short _id){
  
  if( *(int *)buffer[_id][SIZE] == *(int *)buffer[_id][FILLED] ){
    /* we haven't read anything...so MAX_MSG_SIZE is returned */
    return MAX_MSG_SIZE;
  }else if( *(int *)buffer[_id][SIZE] < *(int *)buffer[_id][FILLED] ){
    /* why, i can here you asking... because if we haven't filled SIZE is
       because we have only read 1 BYTE, so we need to read MAX_MSG_SIZE-1 */
    return MAX_MSG_SIZE-1; 
  }else{
    /* then we return the MESSAGE SIZE - strlen(MESSAGE ALREADY READ) */
    return (*(int *)buffer[_id][SIZE] - *(int *)buffer[_id][FILLED]);
  }
}

void NetBuffer::incVar(short &_id){
  /* does it work? */
  if(++_id == BUFFERS_MAX_SIZE)
    _id=0;
  
}


short NetBuffer::readNextBuffer(){
  short id=start;

  while( id != end ){

    if(*(int *)buffer[id][SIZE] == *(int *)buffer[id][FILLED]){
      readBuffer=id;
      return id;
    }
    incVar(id);
  }

  if(id==end){
    if(*(int *)buffer[id][SIZE] == *(int *)buffer[id][FILLED]){
      readBuffer=id;
      return id;
    }
  }
  /* There is no NetBuffer To Read From */
  return -1;
}
