/*
 * $Id$
 */

#include "Wagon.h"

#include "GameController.h"

Wagon::Wagon(GameController* c, WagonType ty, Player* p) :
    GameElement(c, p, ""),
    type(ty)
{
}
