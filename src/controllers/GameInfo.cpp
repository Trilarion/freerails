/*
 * $Id$
 * 'Helper' class that holds basic information about the game
 */

#include "GameInfo.h"

#include "GameController.h"
#include <string.h>

// Contains information about how many days are in each month
const short int daysinmonth[13] =
  { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

GameInfo::GameInfo(GameController* c)
{
  // Duplicated things, GameInfo::init() ???
  controller = c;
  // Init var's to default values
  strcpy(name, ""); // Default name?
  year = 1900;
  month = 1;
  day = 1;
}

GameInfo::GameInfo(GameController* c, char* n, short int y, short int m,
    short int d)
{
  controller = c;
  // FIXME: make deep copy?
  name = n;
  // TODO: check if date is valid
  year = y;
  month = m;
  day = d;
}

GameInfo::~GameInfo()
{
}

void GameInfo::setName(char* n)
{
  strcpy(name, n);
}

void GameInfo::nextDay()
{
  if(day == daysinmonth[month])
  {
    // Next month
    if(month == 12)
    {
      // Next year
      year++;
      month = 1;
      day = 1;
    }
    else
    {
      month++;
      day = 1;
    }
  }
  else
  {
    day++;
  }
}

void GameInfo::setDay(short int d)
{
  if((d >= 1) && (d <= daysinmonth[month]))
    day = d;
}
