/** $Id$
  */
 
#ifndef __SERIALIZEABLE_H__
#define __SERIALIZEABLE_H__

#include "Serializer.h"

class Serializeable {
public:

  virtual void serialize(Serializer* _serializer) {};
  virtual void deserialize(Serializer* _serializer) {};

private:

};

#endif // __SERIALIZEABLE_H__
