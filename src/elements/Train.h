/** @short Class that represents train
  * Train's information is stored in @ref TrainInfo object, that is shared
  * between trains with same type.
  *
  * @author Rivo Laks <rivolaks@hot.ee>
  * @version $Id$ 
  */

#ifndef __TRAIN_H__
#define __TRAIN_H__

#include <vector>

#include "Wagon.h"
#include "TrainInfo.h"
#include "GamePosElement.h"

class Train : public GamePosElement
{
  public:
    /** Constructs train with characteristics given by @ref info
      * Note that you cannot set info to something else later
      */
    Train(unsigned int _posX, unsigned int _posY, TrainInfo* _info, Player* _player);
    ~Train();
    /** Adds wagon to this train */
    void addWagon(Wagon* wagon);
    std::vector<Wagon*> getWagons() { return wagons; };
    /** Returns info object of this train. Note that you cannot set info to
      * something else
      */
    TrainInfo* getInfo() { return info; };

  private:
    std::vector<Wagon*> wagons;
    TrainInfo* info;
};

#endif // __TRAIN_H__
