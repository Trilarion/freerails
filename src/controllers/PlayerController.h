/** $Id$
  */
 
#ifndef __PLAYERCONTROLLER_H__
#define __PLAYERCONTROLLER_H__

#include "Controller.h"
#include "GameElement.h"
#include "Serializer.h"
#include "Player.h"

class PlayerController : public Controller {
public:

  /** Constructor */
  PlayerController();
  /** Destructor */
  virtual ~PlayerController();
  
  GameElement* CreateElement(Serializer* _serializer);

  bool canBuildElement(GameElement* _element) {return true; };
  
private:

};

#endif // __PLAYERCONTROLLER_H__
