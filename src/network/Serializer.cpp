/** $Id$
  */

#include "Serializer.h"
#include "FreeRailsLog.h"
#include <netinet/in.h>

Serializer::Serializer() {
  
  write_buffer=(void *)malloc(sizeof(char)*MAX_MSG_SIZE+1);
  write_counter=0;

  readBuffer=new NetBuffer();
  /*
  read_buffer=(void *)malloc(sizeof(char)*MAX_MSG_SIZE+1);
  read_counter=0;
  write_read_counter=0;
  */
}

Serializer::~Serializer() {
  
  delete readBuffer;

}

inline bool Serializer::enoughSpace(short s)
{
  return (write_counter+s>MAX_MSG_SIZE) ? false:true;  
}

inline bool Serializer::enoughData(short s)
{
  
  return true;

}

void Serializer::initSend(MSG_TYPE msg)
{
  
  memset(write_buffer,'\0',MAX_MSG_SIZE);
  write_counter=0;
  write_int(0); /* message size :) */
  write_char((char) msg);
  
}

size_t Serializer::finishSend(Connection *_myConnection)
{
  int old_counter=write_counter;
  int size=write_counter-sizeof(int);
  write_counter=0; /* rewind buffer */ 
  write_int(size); /* writes real message size :) */
  write_counter=old_counter; /* write_counter points to the end */

  /* memcpy((char *)write_buffer+write_counter,"\0",1); */ /* close buffer */

  return _myConnection->write(write_buffer, write_counter);

}


short Serializer::receive(Connection *_myConnection){
  int size, message_size,buffer;
  
  /* memset(read_buffer, '\0', MAX_MSG_SIZE); */

  /* let's check if it's possible to receive more information */
  if( (buffer=readBuffer->canWriteToBuffer(_myConnection->getConnectionId())) == -1)
    /* no buffers to write to */
    return 0;
  
  size=_myConnection->read(readBuffer->writeToBuffer(), 
			   readBuffer->sizeToWrite(buffer));
  
  readBuffer->written(size);

  /* size=_myConnection->read(&read_buffer+write_read_counter, MAX_MSG_SIZE); */
  
  /*
    write_read_counter+=size;
    if(size > 1){
    // We can read the size of the message
    read_int(message_size);
    if(message_size==size){
    }
    }
  */
}

/*=========================
 * writes
 *========================*/

void Serializer::write_long(long l) {
  
  short size=sizeof(l);
  if(enoughSpace(size)){
    unsigned long x = htonl(l);
    
    memcpy((char *)write_buffer+write_counter,(void *)x,size);
    write_counter+=size;
  }
  
}

void Serializer::write_long_unsigned(long unsigned l) {

  unsigned long x = htonl(l);
  short size=sizeof(l);
  memcpy((char *)write_buffer+write_counter,(void *)x,size);
  write_counter+=size;

  /* return myConnection->write(&l, sizeof(long)); */
}

void Serializer::write_int(int i) {

  unsigned long x = htonl(i);
  short size=sizeof(i);
  memcpy((char *)write_buffer+write_counter,(void *)x,size);
  write_counter+=size;
  /*  return myConnection->write(&i, sizeof(int)); */

}

void Serializer::write_int_unsigned(int unsigned i) {

  unsigned long x = htonl(i);
  short size=sizeof(i);
  memcpy((char *)write_buffer+write_counter,(void *)x,size);
  write_counter+=size;

  /*  return myConnection->write(&i, sizeof(int)); */
}

void Serializer::write_short(short s) {

  unsigned short x = htons(s);
  short size=sizeof(s);
  memcpy((char *)write_buffer+write_counter,(void *)x,size);
  write_counter+=size;

}

void Serializer::write_char(char c) {

  short size=sizeof(c);
  memcpy((char *)write_buffer+write_counter,(void *)c,size);
  write_counter+=size;
  /* return myConnection->write(&c, sizeof(char)); */
}

void Serializer::write_string(const std::string &s) {

  short size = s.length();
  
  if (size) {
    write_short(size);
    memcpy((char *)write_buffer+write_counter,(void*) s.c_str(),size);
    write_counter+=size;
  }
  
}

/*=========================
 * reads
 *========================*/

void Serializer::read_long(long& l) {

  short size=sizeof(long);
  
  if(enoughData(size)){
    unsigned long x;
    
    readBuffer->read((void *)x, size);
    /*
    memcpy(&x,(char *)read_buffer+read_counter,size);
    */

    l=ntohl(x);
    /* read_counter+=size; */
  }
}

void Serializer::read_long_unsigned(long unsigned& l) {

  unsigned long x;
  short size=sizeof(long);
  
  readBuffer->read((void *)x, size);
  /* memcpy(&x,(char *)read_buffer+read_counter,size); */
  l=ntohl(x);
  /* read_counter+=size; */

  /*  return myConnection->read(&l, sizeof(long)); */
}

void Serializer::read_int(int& i) {

  unsigned int x;
  short size=sizeof(int);

  /* memcpy(&x,(char *)read_buffer+read_counter,size); */

  i=ntohl(x);
  /* read_counter+=size; */

  /*return myConnection->read(&i, sizeof(int)); */
}

void Serializer::read_int_unsigned(int unsigned& i) {

  unsigned int x;
  short size=sizeof(int);
  /* memcpy(&x,(char *)read_buffer+read_counter,size); */
  i=ntohl(x);
  /* read_counter+=size; */

  /* return myConnection->read(&i, sizeof(int)); */
}

void Serializer::read_short(short& s) {

  unsigned short x;
  short size=sizeof(short);

  readBuffer->read((void *)x, size);
  /* memcpy(&x,(char *)read_buffer+read_counter,size); */
  s=ntohs(x);
  /* read_counter+=size; */

  /* return myConnection->read(&s, sizeof(short)); */
}

void Serializer::read_char(char& c) {
  
  short size=sizeof(char);
  /* memcpy(&c,(char *)read_buffer+read_counter,size); */
  /* read_counter+=size; */

  /*  return myConnection->read(&c, sizeof(char)); */
}

void Serializer::read_string(std::string& s) {

  short size;
  read_short(size);
  if (size < 0) return;
  char *help_str = new char[size+1];
  if (size) {
    /* s2 = myConnection->read(help_str, size); */
    readBuffer->read((void *)help_str, size);
    /* memcpy(help_str,(char *)read_buffer+read_counter,size); */

  }
  help_str[size] = '\0';
  s = help_str;
}


/*=========================
 * operator <<
 *========================*/

const Serializer& Serializer::operator << (long l) {

  write_long(l);
  return *this;
}

const Serializer& Serializer::operator << (long unsigned l) {

  write_long_unsigned(l);
  return *this;
}

const Serializer& Serializer::operator << (int i) {

  write_int(i);
  return *this;
}

const Serializer& Serializer::operator << (int unsigned i) {

  write_int_unsigned(i);
  return *this;
}

const Serializer& Serializer::operator << (short s) {

  write_short(s);
  return *this;
}

const Serializer& Serializer::operator << (char c) {

  write_char(c);
  return *this;
}

const Serializer& Serializer::operator << (const std::string &s) {

  write_string(s);
  return *this;
}

/*=========================
 * operator >>
 *========================*/

const Serializer& Serializer::operator >> (long& l) {

  read_long(l);
  return *this;
}

const Serializer& Serializer::operator >> (long unsigned& l) {

  read_long_unsigned(l);
  return *this;
}

const Serializer& Serializer::operator >> (int& i) {

  read_int(i);
  return *this;
}

const Serializer& Serializer::operator >> (int unsigned& i) {

  read_int_unsigned(i);
  return *this;
}

const Serializer& Serializer::operator >> (short& s) {

  read_short(s);
  return *this;
}

const Serializer& Serializer::operator >> (char& c) {

  read_char(c);
  return *this;
}

const Serializer& Serializer::operator >> (std::string& s) {

  read_string(s);
  return *this;
}
