#ifndef __WAGON_H__
#define __WAGON_H__

#include "GameElement.h"
/** Class that represents wagon
  */
class Wagon: public GameElement
{
  public:
    /** Constructs new wagon, which will be connected to train
      * Wagon will be positioned after wagon previous
      * If wagon previous is NULL, then this wagon will be immediatly after
      * train
      */
    Wagon(Train* train, WagonType type, Wagon* previous = 0l);
    ~Wagon();
    /** Sets next wagon to next
      * WARNING: This method may be useless, if we'll use vector to store wagons
      * of train in train element
      */
    void setNext(Wagon* n) { next = n; };
    /** Returns pointer to next wagon
      * WARNING: This method may be useless, if we'll use vector to store wagons
      * of train in train element
      */
    Wagon* getNext() { return next; };
    /** If this is last wagon returns true, else false */
    bool isLast() { if(next == 0l) return true; else return false; };
    /** Returns pointer to train */
    Train* getTrain() { return train; };
    /** Enumeration of wagon types
	  * Currently has all wagon types from both America and Europe (from
	  * http://freerails.sourceforge.net/elements.html)
	  */
    enum WagonType { Mail = 0, Passenger, Wine, Grape, Armament, Fertilizer,
        Textile, Steel, Nitrate, Wool, Coal, Caboose, Beer, Livestock, Goods,
        Hop, Chemicale, Cotton };
    /** Return type of wagon */
    WagonType getType() { return type; };
    /** Sets type of wagon */
    void setType(WagonType t) { type = t; };
  private:
    Train* train;
    Wagon* next;
    WagonType type;
};

#endif // __WAGON_H__
