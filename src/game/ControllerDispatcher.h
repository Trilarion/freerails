/** $Id$
  * The class holds all Controllers by there typeID in a map.
  * Normaly only the Engine add's new Controllers to the Dispatcher.
  */
 
#ifndef __CONTROLLERDISPATCHER_H__
#define __CONTROLLERDISPATCHER_H__

#include "Controller.h"
#include <map>

class ControllerDispatcher {
public:

  /** Constructor */
  ControllerDispatcher();
  /** Destructor */
  virtual ~ControllerDispatcher();
  
  void addController(Controller* _controller);
  
  Controller* getController(GameElement::TypeID _typeID);
private:

  std::map<GameElement::TypeID,Controller*> controllerMap;

};

#endif // __CONTROLLERDISPATCHER_H__
