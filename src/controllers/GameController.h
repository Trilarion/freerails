/*
 * $Id$
 * GameController class
 */

#ifndef __GAMECONTROLLER_H__
#define __GAMECONTROLLER_H__

#include <vector>
#include <map>

class GameElement;
class Player;
class Station;
class GameInfo;

// Type of ID's
typedef long unsigned int idtype;

/** @short Class, that controls the game
  * GameController is one of the main classes in game.
  * It has access to all objects of game (trains, stations ...) and to all
  * players. All orders are sent to this class.
  * Maps update from this class.
  * When user input is received, it is sent to this class for analysing
  * Simpler data of this class is stored in GameInfo object, which can be
  * received using @ref getInfo(). Data in GameInfo cannot be modified by
  * anyone, but GameController
  *
  * @author Rivo Laks <rivolaks@hot.ee>
  * @version $Id$
  */
class GameController
{
  public:
    /** Construct GameController
      * To initialize data, @ref init() must be called later
      */
    GameController();
    /** Construct GameController and initialize it
      * Parameters are same as for @ref init()
      */
    GameController(char* gamename, short int year, short int month,
        short int day);
    /** Destructs GameController */
    ~GameController();

    /** Initialize GameController
      * This can be used only once and after that, important data (like current
      * date) cannot be changed anymore
      * If it is used for second time, it immediately returns false
      * If used for first time, and everything's OK, returns true
      * @param gamename Name of created game
      * @param year Starting year of game
      * @param month Starting month of game
      * @param day Starting day of game
      */
    bool init(char* gamename, short int year, short int month, short int day);

    /** Returns GameInfo class, that contains information about running game
      * You can't set GameInfo
      */
    GameInfo* getInfo() { return info; };
    /** Returns GameInfo class, that contains start information about current
      * game. In this object year() returns year, when game was started, not
      * current year etc.
      */
    GameInfo* getStartInfo() { return startinfo; };

    /** Adds element to the game
      * It does not update the map immediately
      * It returns ID given to element or 0 when error occures
      */
    idtype addElement(GameElement* e);
    /** Removes element from the game
      * Like @ref addElement(), it does not update the map
      */
    void removeElement(idtype id);

    /** Starts the game. To start the game, GameController must be initialized.
      * If it isn't, then this method returns false immediately. If everything
      * goes fine, it returns true
      */
    bool startGame();

  private:
    vector<Player*> players;
    vector<GameElement*> elements;
    // Stations needs to be processed first
    vector<Station*> stations;

    GameInfo* info;
    GameInfo* startinfo;

    bool inited;

    // Will 4 millon id's be enough ???
    map<idtype, GameElement*> idmap;
    idtype lastid;
};

#endif // __GAMECONTROLLER_H__
