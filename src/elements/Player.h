/** $Id$
  * Base class for the players (Human, AI, Nature)
  */

#ifndef __PLAYER_H__
#define __PLAYER_H__

#include "GameElement.h"
#include <vector>

class Player {
public:
  /** Constructor
    * @param n: name of this player */
  Player(char* n);
  /** Destructor */
  virtual ~Player();
  /** Gets the name of the player */
  char* getName();
  /** Adds a gameElement
    * @param element: GameElement to add */
  void addGameElement(GameElement* element);

private:
  /** Name of the player */
  char* name;
  /** Vector of GameElements* */
  vector<GameElement*> elements;

};

#endif // __PLAYER_H__