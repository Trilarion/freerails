/** $Id$
  * Base class for ALL the elements in the game
  */
 
#ifndef __GAMEELEMENT_H__
#define __GAMEELEMENT_H__

#include "GameController.h"

class Player;

class GameElement {
public:
  /** Constructor
    * @param p: Player who own this element
    * @param filename: location where the tileset is located
    * TODO: format of filename for animated tileset??
    */
  GameElement(GameController* c, Player* p, char* filename);
  /** Destructor */
  virtual ~GameElement();
  /** Changes the owner of this element */
  void setPlayer(Player* p);
  /** Return the owner of this element */
  Player* getPlayer();
  /** Update the current tile, etc... */
  virtual void update();
  /** Return type of element */
  virtual int rtti() = 0;
  /** Register element in GameController
    * Element can be registered only once
    * If everything goes well, it returns true, otherwise false
    */
  // We can't use just 'register()' because it's keyword :(
  bool registerSelf();
  /** Returns ID of this element
    * If ID is 0 then it means that element is not registered or there was
    * error when registering
    */
  idtype getId() { return id; };

private:
  /** Player who owns this element */
  Player* player;
  /** Location of the tileset
    * TODO: which format?? */
  char* tileset;
  /** ID of element */
  idtype id;
  bool registered;
  /** Pointer to GameController */
  GameController* controller;
};

#endif // __GAMEELEMENT_H__
