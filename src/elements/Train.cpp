/*
 * $Id$
 */

#include "Train.h"

Train::Train(TrainInfo* i, Player* p) : GameElement(p, "")
{
  info = i;
}

Train::~Train()
{
  // Delete wagons
  for(int i = 0; i < wagons.size(); i++)
    delete wagons[i];
}

void Train::addWagon(Wagon* w)
{
  wagons.push_back(w);
}
