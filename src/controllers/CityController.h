/** $Id$
  */
 
#ifndef __CITYCONTROLLER_H__
#define __CITYCONTROLLER_H__

#include "Controller.h"
#include "GameElement.h"
#include "Serializer.h"
#include "City.h"
#include "WorldMap.h"

class CityController : public Controller {
public:

  /** Constructor */
  CityController(WorldMap* _worldMap);
  /** Destructor */
  virtual ~CityController();
  
  GameElement* CreateElement(Serializer* _serializer);

  bool canBuildElement(GameElement* _element) {return true; };
  
private:

};

#endif // __CITYCONTROLLER_H__
