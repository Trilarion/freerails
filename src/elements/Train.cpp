/*
 * $Id$
 */

#include "Train.h"

#include "GameController.h"

Train::Train(GameController* c, TrainInfo* i, Player* p) : GameElement(c, p, "")
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
