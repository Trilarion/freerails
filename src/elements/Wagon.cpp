/*
 * $Id$
 */

#include "Wagon.h"

Wagon::Wagon(Train* tr, WagonType ty, Wagon* previous) :
    train(tr),
    type(ty)
{
  if(previous == 0l)
  {
    // This wagon is next to train
    next = train->firstWagon();
  }
  else
  {
    next = previous->getNext();
    previous->setNext(this);
  }
  train->addWagon(this);
}
