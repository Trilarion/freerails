/*
 * $Id$
 */

#include "Train.h"

Train::Train(TrainInfo* _info, Player* _player)
     : GameElement(_player, idTrain)
{
  info = _info;
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
