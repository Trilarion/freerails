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

    enum PlayerType {NATUR=0, AI, HUMAN};
    /** Constructor
      * @param n: name of this player */
    Player(std::string _name, PlayerType _playerType);
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
    PlayerType getType() {return playerType;};
    /** Sets the type of the player */
    void setType(PlayerType _playerType) {playerType = _playerType;};

    // get amount of money for player
    double getMoney();
    // change amount of money for player
    double incMoney(double _money);
    
  private:
    /** Name of the player */
    std::string name;
    PlayerType playerType;
    double money;
};

#endif // __PLAYER_H__
