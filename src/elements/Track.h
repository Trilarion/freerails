/** @short Class that represents a track
  *
  * @author Alexander Opitz <opitz@primacom.net>
  * @version $Id$
  */

#ifndef __TRACK_H__
#define __TRACK_H__

#include "GameElement.h"
#include "Player.h"

class GameController;

class Track : public GameElement
{
  public:
    /** Constructs a track */
    Track(GameController* _controller, Player* _player);
    ~Track();

  private:
};

#endif // __TRACK_H__
