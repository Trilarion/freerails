/*
 * $Id$
 */

#include "Wagon.h"

#include "GameController.h"

Wagon::Wagon(GameController* c, WagonType ty, Player* p) :
    GameElement(p, 4),
    type(ty)
{
}

Wagon::~Wagon()
{
}
