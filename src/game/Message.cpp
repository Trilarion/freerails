/** $Id$
  */

#include "Message.h"

Message::Message(int _type, void* _data) {

  type=_type;
  data=_data;
}

Message::~Message() {

}