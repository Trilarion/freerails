/*
 * $Id$
 * GameController class
 * !!!!!!!!!!!!!! Will be removed laterly!!!!!!!!!!!!!
 */

#ifndef __GAMECONTROLLER_H__
#define __GAMECONTROLLER_H__

#include <vector>
#include <map>

class GameElement;
class Player;
class Station;
class GameInfo;
class WorldMap;

// Type of ID's
typedef long unsigned int idtype;

/** @short Class that controls the game
  *
  * GameController is one of the main classes in game.
  * It has access to all objects of game (trains, stations ...) and to all
  * players. All orders are sent to this class.
  * Maps update from this class.
  * When user input is received, it is sent to this class for analysing.
  * Simpler data of this class is stored in GameInfo object, which can be
  * received using @ref getInfo(). Data in GameInfo cannot be modified by
  * anyone, but GameController.
  *
  * @see GameInfo
  *
  * @author Rivo Laks <rivolaks@hot.ee>
  * @version $Id$
  */
class GameController
{
  public:
    /** Describes state of the game
      * @li NotStarted means that game has not been started yet
      * @li Running means that game is running at the moment
      * @li Paused means that game is temporarily paused
      * @li Stopped means that game was started, but is stopped at the moment
      */
    enum GameState { NotStarted = 0, Running, Paused, Stopped };
    /** Construct GameController
      * To initialize data, @ref init() must be called later
      */
    GameController();
    /** Construct GameController and initialize it
      * Parameters are same as for @ref init()
      * WARNING: API of this method will change!
      */
    GameController(char* gamename, short int year, short int month,
        short int day);
    /** Destructs GameController
      * If game is running or paused, then it stops game and deletes game
      * information. If game is @ref init()'ed, then it deletes GameInfo
      * objects
      */
    ~GameController();

    /** Initialize GameController
      * It can only be used after @ref stopGame() or after calling constructor,
      * that doesn't initialize game.
      * If it is used for second time, it immediately returns false
      * If used for first time, and everything's OK, returns true
      * WARNING: API of this method will change!
      * @param gamename Name of created game
      * @param year Starting year of game
      * @param month Starting month of game
      * @param day Starting day of game
      */
    bool init(char* gamename, short int year, short int month, short int day);
    /** Returns true if GameController is initialized
      * You can initialize GameController with @ref init() or in constructor
      */
    bool isInited() { return inited; };

    /** Returns GameInfo class, that contains information about running game
      * You can't set GameInfo
      */
    GameInfo* getInfo() { return info; };
    /** Returns GameInfo class, that contains start information about current
      * game. In this object year() returns year, when game was started, not
      * current year etc.
      */
    GameInfo* getStartInfo() { return startinfo; };

    /** Starts the game. To start the game, GameController must be initialized.
      * If it isn't, then this method returns false immediately. If everything
      * goes fine, it returns true
      */
    bool startGame();
    /** Pauses or unpauses the game. If game is running, then it pauses it, if
      * it is already paused then it unpauses it, and otherwise it does
      * nothing.
      */
    void pauseGame();
    /** Stops the game. WARNING: This deletes ALL information associated with
      * game. To start new game after stopping, you must call @ref init()
      */
    void stopGame();
    /** Returns current state of game */
    GameState getState() { return state; };

    /** Adds element to the game
      * It does not update the map immediately
      * It returns ID given to element or 0 when error occures
      */
    bool addPlayer(Player* p);
    /** Returns vector containing pointers to all players */
    std::vector<Player*> getPlayers() { return players; };

    // return a pointer to worldmap
    WorldMap* getWorldMap() {return map;};

  private:
    // TODO: set capacity()'s
    std::vector<Player*> players;
    std::vector<GameElement*> elements;
    // Stations needs to be processed first
    std::vector<Station*> stations;

    GameInfo* info;
    GameInfo* startinfo;

    bool inited;

    // Will 4 millon id's be enough ???
    std::map<idtype, GameElement*> idmap;
    idtype lastid;

    GameState state;

    // TODO: API for map things (When map's API will be stable)
    WorldMap* map;
};

#endif // __GAMECONTROLLER_H__
