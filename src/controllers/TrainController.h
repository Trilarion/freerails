/**
  * This class is handling build, remove and move of trains.
  * @author Alexander Opitz
  * @version $Id$
  */
#ifndef __TRAINCONTROLLER_H_
#define __TRAINCONTROLLER_H_

#include "Controller.h"
#include "Train.h"
#include "TrainInfo.h"
#include "WorldMap.h"

class TrainController : public Controller
{
  
  public: 
    TrainController(WorldMap *_map);
    ~TrainController();

    GameElement* CreateElement(Serializer* _serializer);
    bool canBuildElement(GameElement* _element);
    void addGameElement(GameElement* _element);
    void removeGameElement(long int _elementID);

  private:
    bool testBuildElement(int x, int y);
    void trainDoBuild(Train* train);
};

#endif
