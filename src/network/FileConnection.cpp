/*
 * $Id$
 */

#include "FileConnection.h"

FileConnection::FileConnection():Connection() {

}


FileConnection::~FileConnection() {

  close();

}

void FileConnection::open(char* filename, Mode stat) {
  
  mode=stat;
  
  if(stat==READ){
    file=new fstream(filename,fstream::in);
  }else if(stat==OVERWRITE){
    file=new fstream(filename,fstream::app|fstream::trunc);
  }else if(stat==BACKUP){
    /* TODO: RENAME OLD FILE */
    file=new fstream(filename,fstream::app|fstream::trunc);
  }else if(stat==APPEND)
    file=new fstream(filename,fstream::app);
  else{
    /* throw some exception */
    
  }

  if(file->is_open())
    state=OPEN;
}



void FileConnection::close() {

  if (state!=IDLE)
  {
    state=CLOSING;
    file->close();
  }
  state=IDLE;
}

int FileConnection::writeTo(void* data, int len) {
  error=NONE;

  if(mode!=READ){
    if (state==OPEN){
      if(file->ios::good()){     /* Check if stream is good for i/o operations. */
	file->write(data,len);
	if(file->ios::fail()){
	  error=OTHER;  /* write error */
	  return -1;  
	} 
	return 0;
      }else{
	error=OTHER;
	return -1;
      }
    } else{
      error=NOT_OPEN;
      return -1;
    }
  }else{
    /* FILE OPENED TO READ FROM */
    error=REFUSED;
    return -1;
  }
}

int FileConnection::readFrom(void* buf, int maxlen) {
  
  if(mode==READ){
    if (state==OPEN){
      if(file->ios::good()){     /* Check if stream is good for i/o operations. */
	file->read(buf,maxlen);
	if (file->ios::fail()){
	  error=OTHER;  /* read error */
	  return -1;  
	}
	return 0;
      }else{
	error=OTHER;
	return -1;
      }
    }else{
      error=NOT_OPEN;
      return -1;
    }
  }else{
    /* FILE OPENED TO WRITE TO */
    error=REFUSED;
    return -1;
  }
}


bool FileConnection::endOfFile(){
  return file->ios::eof();  /* we could throw an exception if something goes wrong */
}

