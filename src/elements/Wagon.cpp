/*
 * $Id$
 */

#include "Wagon.h"

Wagon::Wagon(WagonType ty, Player* p) :
    GameElement(p, ""),
    type(ty)
{
}
