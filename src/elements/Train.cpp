/*
 * $Id$
 */

#include "Train.h"
#include <iostream>

Train::Train(unsigned int _posX, unsigned int _posY, TrainInfo* _info, Player* _player)
     : GamePosElement(_posX, _posY, _player, idTrain)
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
