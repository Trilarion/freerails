/*
 * $Id$
 */

#include "Wagon.h"

Wagon::Wagon(WagonType ty, Player* _player)
     : GameElement(_player, idWagon),
    type(ty)
{
}

Wagon::~Wagon()
{
}
