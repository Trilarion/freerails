/** $Id$
  * Base class for the players (Human, AI, Nature)
  */

#ifndef __PLAYER_H__
#define __PLAYER_H__

#include <vector>
#include <string>

#include "GameElement.h"
#include "Serializer.h"

class Player : public GameElement {
public:

  enum Type {NATUR=0, AI, HUMAN};
  /** Constructor
    * @param n: name of this player */
  Player();
  Player(string _name, Type _type);
  /** Destructor */
  virtual ~Player();
  
  /** Serialization */
  void serialize(Serializer* _serializer);
  void deserialize(Serializer* _serializer);

  /** Gets the name of the player */
  string getName() {return name;};
  /** Sets the name of the player */
  void setName(string _name) {name = _name;};

  /** Gets the type of the player */
  Type getType() {return type;};
  /** Sets the type of the player */
  void setType(Type _type) {type = _type;};

private:
  /** Name of the player */
  string name;
  Type type;
};

#endif // __PLAYER_H__