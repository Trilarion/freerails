/*
 * $Id$
 * Wagon class
 */
#ifndef __WAGON_H__
#define __WAGON_H__

#include "GameElement.h"

/** @short Class that represents wagon
  *
  * @author Rivo Laks <rivolaks@hot.ee>
  * @version $Id$
  */
class Wagon : public GameElement
{
  public:
    /** Enumeration of wagon types
      * Currently has all wagon types from both America and Europe (from
      * http://freerails.sourceforge.net/elements.html)
      */
    enum WagonType { Mail = 0, Passenger, Wine, Grape, Armament, Fertilizer,
        Textile, Steel, Nitrate, Wool, Coal, Caboose, Beer, Livestock, Goods,
        Hop, Chemicale, Cotton };
    /** Constructs new wagon
      * Wagon must be positioned by train that constructed it
      */
    Wagon(WagonType type, Player* _player);
    ~Wagon();
    /** Return type of wagon */
    WagonType getType() { return type; };
    /** Sets type of wagon */
    void setType(WagonType t) { type = t; };

  private:
    WagonType type;
};

#endif // __WAGON_H__
