/*
 * $Id$
 */

#include "Train.h"

#include "GameController.h"

Train::Train(GameController* c, TrainInfo* i, Player* p)
     : GameElement(p, idTrain)
{
  info = i;
}

Train::~Train()
{
  // Delete wagons
  for(unsigned int i = 0; i < wagons.size(); i++)
    delete wagons[i];
}

void Train::addWagon(Wagon* w)
{
  wagons.push_back(w);
}
