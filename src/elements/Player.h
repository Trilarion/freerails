/** $Id$
  * Base class for the players (Human, AI, Nature)
  */

#ifndef __PLAYER_H__
#define __PLAYER_H__

#include <vector>
#include <string>

#include "GameElement.h"
#include "Serializer.h"

class Player : public GameElement
{
  public:

    enum Type {NATUR=0, AI, HUMAN};
    /** Constructor
      * @param n: name of this player */
    Player(std::string _name, Type _type);
    /** Destructor */
    ~Player();

    /** Serialization */
    void serialize(Serializer* _serializer);
    void deserialize(Serializer* _serializer);

    /** Gets the name of the player */
    std::string getName() {return name;};
    /** Sets the name of the player */
    void setName(std::string _name) {name = _name;};

    /** Gets the type of the player */
    Type getType() {return type;};
    /** Sets the type of the player */
    void setType(Type _type) {type = _type;};

    // get amount of money for player
    double getMoney();
    // change amount of money for player
    double incMoney(double _money);
    
  private:
    /** Name of the player */
    std::string name;
    Type type;
    double money;
};

#endif // __PLAYER_H__
