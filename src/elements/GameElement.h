/** $Id$
  * Base class for ALL the elements in the game
  */
 
#ifndef __GAMEELEMENT_H__
#define __GAMEELEMENT_H__

#include "Player.h"

class Player;

class GameElement {
public:
  /** Constructor
    * @param p: Player who own this element
    * @param filename: location where the tileset is located
    * TODO: format of filename for animated tileset??
    */
  GameElement(Player* p, char* filename);
  /** Destructor */
  virtual ~GameElement();
  /** Changes the owner of this element */
  void setPlayer(Player* p);
  /** Return the owner of this element */
  Player* getPlayer();
  /** Update the current tile, etc... */
  virtual void update();
  

private:
  /** Player who owns this element */
  Player* player;
  /** Location of the tileset
    * TODO: which format?? */
  char* tileset;
};

#endif // __GAMEELEMENT_H__