/** $Id$
  */

#include "Serializer.h"

Serializer::~Serializer() {
}

/*=========================
 * writes
 *========================*/

size_t Serializer::write_long(long l) {

  return myConnection->write(&l, sizeof(long));
}

size_t Serializer::write_long_unsigned(long unsigned l) {

  return myConnection->write(&l, sizeof(long));
}

size_t Serializer::write_int(int i) {

  return myConnection->write(&i, sizeof(int));
}

size_t Serializer::write_short(short s) {

  return myConnection->write(&s, sizeof(short));
}

size_t Serializer::write_char(char c) {

  return myConnection->write(&c, sizeof(char));
}

size_t Serializer::write_string(const std::string &s) {

  short size = s.length();
  size_t s1 = write_short(size);
  if (s1<0) return s1;
  size_t s2 = 0;
  if (size) {
    s2 = myConnection->write((void*) s.c_str(), size);
    if (s2<0) return s2;
  }
  return s1+s2;
}

/*=========================
 * reads
 *========================*/

size_t Serializer::read_long(long& l) {

  return myConnection->read(&l, sizeof(long));
}

size_t Serializer::read_long_unsigned(long unsigned& l) {

  return myConnection->read(&l, sizeof(long));
}

size_t Serializer::read_int(int& i) {

  return myConnection->read(&i, sizeof(int));
}

size_t Serializer::read_short(short& s) {

  return myConnection->read(&s, sizeof(short));
}

size_t Serializer::read_char(char& c) {

  return myConnection->read(&c, sizeof(char));
}

size_t Serializer::read_string(std::string& s) {

  short size;
  size_t s1 = read_short(size);
  if (s1 < 0) return 0;
  char help_str[size+1];
  size_t s2 = 0;
  if (size) {
    s2 = myConnection->read(help_str, size);
    if (s2 < 0) return 0;
  }
  help_str[size] = '\0';
  s = help_str;
  return s1 + s2;
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
