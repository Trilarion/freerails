/*
 * $Id$
 * Class that holds information about a train type
 */

#ifndef __TRAININFO_H__
#define __TRAININFO_H__

/** @short Class that holds information about a train type
  * This class should be shared between train objects e.g. trains have
  * pointers to this object of this class. This way when train's
  * characteristics changes, it also automatically affects all trains.
  * For example all trains should go a bit cheaper every year.
  *
  * @author Rivo Laks <rivolaks@hot.ee>
  * @version $Id$
  */
class TrainInfo
{
  public:
    /** Type of train: steam, diesel or electric */
    enum TrainType { Steam = 0, Diesel, Electric };
    /** Constructs TrainInfo with empty values */
    TrainInfo();
    /** Constructs TrainInfo with given values
      * @param name Name of train
      * @param year Year of train
      * @param type Type of train
      * @param price Price of train
      * @param fuelcost Cost of fuel for train (per month)
      */
    TrainInfo(char* name, TrainType type, short int year, long int price,
        long int fuelcost);
    /** Returns name of train */
    char* getName() { return name; };
    /** Sets name of train */
    void setName(char* n);
    /** Returns year of train */
    short int getYear() { return year; };
    /** Sets year of train */
    void setYear(short int y) { year = y; };
    /** Returns type of train */
    TrainType getType() { return type; };
    /** Sets type of train */
    void setType(TrainType t) { type = t; };
    /** Returns price of train */
    long int getPrice() { return price; };
    /** Sets price of train */
    void setPrice(long int p) { price = p; };
    /** Returns cost of fuel per month */
    long int getFuelCost() { return fuelcost; };
    /** Sets fuel cost of train */
    void setFuelCost(long int f) { fuelcost = f; };
  private:
    char* name;
    short int year;
    TrainType type;
    long int price;
    long int fuelcost;
};

#endif // __TRAININFO_H__
