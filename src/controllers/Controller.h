/** $Id$
  */
 
#ifndef __CONTROLLER_H__
#define __CONTROLLER_H__

#include <map>

#include "GameElement.h"
#include "Serializer.h"

class WoldMap;

class Controller
{
  public:

    /** Constructor */
    Controller(WorldMap *_map, GameElement::TypeID _typeID);
    /** Destructor */
    virtual ~Controller();

    void addGameElement(GameElement* _element);
    void removeGameElement(long int _elementID);
    GameElement* getGameElement(long int _elementID);

    GameElement::TypeID getTypeID() {return typeID;};
 
    // create an element
    virtual GameElement* CreateElement(Serializer* _serializer) = 0;

    // can a specific element be build
    virtual bool canBuildElement(void *, int *, int *, int *) = 0;

  protected:
    // compute direction inner field of a given point relative to center of field
    int computeDirection(int x, int y);

    GameController *controller;
    WorldMap *worldMap;
        
  private:

    GameElement::TypeID typeID;

    std::map<long int, GameElement*> elementMap;
  
};

#endif // __CONTROLLER_H__
