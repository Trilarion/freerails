/** $Id$
  * Base class for ALL the elements in the game which needs Positions
  */
 
#ifndef __GAMEPOSELEMENT_H__
#define __GAMEPOSELEMENT_H__

#include "GameElement.h"
#include "Serializeable.h"

class Player;

class GamePosElement : public GameElement
{
  public:
    /** Constructor
      * @param _posX, _posY: Position of element
      */
    GamePosElement(unsigned int _posX, unsigned int _posY, Player* _player, TypeID _typeID);
    /** Destructor */
    virtual ~GamePosElement();

    /** Serialization */
    void serialize(Serializer* _serializer);
    void deserialize(Serializer* _serializer);

  private:

    /** Position of the element */
    unsigned int posX;
    unsigned int posY;
};

#endif // __GAMEPOSELEMENT_H__
