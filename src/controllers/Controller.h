/** $Id$
  */
 
#ifndef __CONTROLLER_H__
#define __CONTROLLER_H__

#include <map>

#include "GameElement.h"
#include "Serializer.h"

class Controller {
public:

  /** Constructor */
  Controller(int _typeID);
  /** Destructor */
  virtual ~Controller();
  
  void addGameElement(GameElement* _element);
  void removeGameElement(long int _elementID);
  GameElement* getGameElement(long int _elementID);
  
  int getTypeID() {return typeID;};
  
  virtual GameElement* CreateElement(Serializer* _serializer) {return NULL;};
  virtual bool canBuildElement(GameElement* _element) {return false;};

private:

  int typeID;
  
  map<long int, GameElement*> elementMap;

};

#endif // __CONTROLLER_H__