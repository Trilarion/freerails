/*
 * $Id$
 * 'Helper' class for GameController that holds basic information about game
 */

#ifndef __GAMEINFO_H__
#define __GAMEINFO_H__

class GameController;

/** @short 'Helper' class that holds basic information about game
  * It holds basic information about game, like game's name and current year.
  * Only get methods are public, so other classes can read game's name and
  * year, but can't set it. Exception is GameController class, which is our
  * friend and has access to everything.
  * Reason behind this class is, that other parts of game, which needs to
  * read game's date for example, can hold pointer to this class and read from
  * it. It's API is smaller than GameController's, too.
  *
  * @author Rivo Laks <rivolaks@hot.ee>
  * @version $Id$
  */
class GameInfo
{
  // GameController can do enything with us
  friend class GameController;
  public:
    /** Constructs new GameInfo, which is child of GameController @ref gc
      * Name and date information must be set afterwards
      */
    GameInfo(GameController* gc);
    /** Constructs new GameInfo, which is child of GameController @ref gc
      * Games name will be set to @ref name, date is set by @ref year,
      * @ref month and @ref day that defaults to 1st Jan 1900
      */
    GameInfo(GameController* gc, char* name, short int year = 1900,
        short int month = 1, short int day = 1);
    ~GameInfo();
    /** Returns name of game */
    char* getName();
    /** Returns current year of game */
    short int getYear();
    /** Returns number of current month of game */
    short int getMonth();
    /** Returns current day of game */
    short int getDay();
    /** Returns GameController class to which this object belongs to */
    GameController* getController() { return controller; };
  private:
    // Contains information about how many days are in each month
    const short int daysinmonth[];
    /** Sets name of game. Meant to be used by GameController only */
    void setName(char* n);
    /** Sets year of game. Meant to be used by GameController only */
    void setYear(short int y) { year = y; };
    /** Sets month of game. Meant to be used by GameController only */
    void setMonth(short int m) { if((m >= 1) && (m <= 12)) month = m; };
    /** Sets day of game. Meant to be used by GameController only */
    void setDay(short int d);
    /** Increases day by one. If needed increases month and year, too.
      * Meant to be used by GameController only
      */
    void nextDay();
    char* name;
    // These would be stored as one number like in QDate somehow...
    short int year;
    short int month;
    short int day;
    GameController* controller;
};

#endif // __GAMEINFO_H__
