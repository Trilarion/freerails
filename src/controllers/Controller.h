/** $Id$
  */
 
#ifndef __CONTROLLER_H__
#define __CONTROLLER_H__

#include <map>
#include <iostream>
// For std::cerr messages in the controller classes

#include "GameElement.h"
#include "Serializer.h"
#include "WorldMap.h"

class Controller
{
  public:

    /** Constructor */
    Controller(WorldMap *_map, GameElement::TypeID _typeID);
    /** Destructor */
    virtual ~Controller();

    virtual void addGameElement(GameElement* _element);
    void removeGameElement(long int _elementID);
    GameElement* getGameElement(long int _elementID);

    std::map<GameElement::ElementID, GameElement*> getGameElements() { return elementMap; };
    GameElement::TypeID getTypeID() {return typeID;};
 
    // create an element
    virtual GameElement* CreateElement(Serializer* _serializer) = 0;

    // can a specific element be build
    virtual bool canBuildElement(GameElement* _element) = 0;

  protected:
    WorldMap *worldMap;
        
  private:

    GameElement::TypeID typeID;
    GameElement::ElementID highestElementID;

    std::map<GameElement::ElementID, GameElement*> elementMap;
  
};

#endif // __CONTROLLER_H__
