/*
 * $Id$
 * Train class
 */

#ifndef __TRAIN_H__
#define __TRAIN_H__

#include <vector.h>

#include "Wagon.h"
#include "TrainInfo.h"
#include "GameElement.h"
#include "Player.h"

class GameController;

#define RTTI_TRAIN 2

/** @short Class that represents train
  * Train's information is stored in @ref TrainInfo object, that is shared
  * between trains with same type.
  *
  * @author Rivo Laks <rivolaks@hot.ee>
  * @version $Id$ 
  */
class Train : public GameElement
{
  public:
    /** Constructs train with characteristics given by @ref info
      * Note that you cannot set info to something else later
      */
    Train(GameController* c, TrainInfo* info, Player* p);
    ~Train();
    /** Adds wagon to this train */
    void addWagon(Wagon* wagon);
    /** Returns info object of this train. Note that you cannot set info to
      * something else
      */
    TrainInfo* getInfo() { return info; };
    /** Return type of this element (RTTI_TRAIN) */
    virtual int rtti() { return RTTI_TRAIN; };
  private:
    vector<Wagon*> wagons;
    TrainInfo* info;
};

#endif // __TRAIN_H__
