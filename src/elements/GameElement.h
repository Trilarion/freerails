/** $Id$
  * Base class for ALL the elements in the game
  */
 
#ifndef __GAMEELEMENT_H__
#define __GAMEELEMENT_H__

#include "Serializeable.h"

class Player;

class GameElement : public Serializeable
{
  public:
    enum TypeID { idNone = 0, idPlayer, idStation, idTrain, idWagon, idTrack, idCity };
    typedef long unsigned int ElementID;
    /** Constructor
      * @param _player: Player who own this element
      * @param _typeID: The type from which this element is
      */
    GameElement(Player* _player, TypeID _typeID);
    /** Destructor */
    virtual ~GameElement();

    /** Serialization */
    void serialize(Serializer* _serializer);
    void deserialize(Serializer* _serializer);

    /** Return type of element */
    ElementID getElementID() {return elementID;};
    void setElementID(ElementID _elementID) {elementID=_elementID;};
    TypeID getTypeID() {return typeID;};

    /** Changes the owner of this element */
    void setPlayer(Player* p);
    /** Return the owner of this element */
    Player* getPlayer();
    /** Update the current tile, etc... */
    virtual void update();

  private:
    /** Player who owns this element */
    Player* player;

    ElementID elementID;
    TypeID typeID;
};

#endif // __GAMEELEMENT_H__
