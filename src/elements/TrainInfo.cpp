/*
 * $Id$
 */

#include <string.h>

#include "TrainInfo.h"

TrainInfo::TrainInfo()
{
  strcpy(name, "");
  type = Steam; // It must be something
  year = 0;
  price = 0;
  fuelcost = 0;
}

TrainInfo::TrainInfo(char* n, TrainType t, short int y, long int p,
    long int f)
{
  strcpy(name, n);
  type = t;
  year = y;
  price = p;
  fuelcost = f;
}

void TrainInfo::setName(char* n)
{
  strcpy(name, n);
}
