/** @short Class that represents a track
  *
  * @author Alexander Opitz <opitz@primacom.net>
  * @version $Id$
  */

#ifndef __TRACK_H__
#define __TRACK_H__

#include "GameElement.h"

class GameController;
class Player;

#define TrackGoNorth            0x0001
#define TrackGoNorthEast        0x0002
#define TrackGoEast             0x0004
#define TrackGoSouthEast        0x0008
#define TrackGoSouth            0x0010
#define TrackGoSouthWest        0x0020
#define TrackGoWest             0x0040
#define TrackGoNorthWest        0x0080

#define TrackIsStation          0x0100
#define TrackIsBridge           0x0200
#define TrackIsSignal           0x0400

#define TrackHasCornerNorthEast 0x1000
#define TrackHasCornerSouthEast 0x2000
#define TrackHasCornerSouthWest 0x4000
#define TrackHasCornerNorthWest 0x8000

class Track : public GameElement
{
  public:
    /** Constructs a track */
    Track(GameController* _controller, Player* _player);
    ~Track();

    unsigned short getConnect() { return connect; };
    void setConnect(unsigned short _con);

    void getTrackTile(int i, int *x, int *y);

  private:
    GameController *controller;
                           
    unsigned short connect;   // connection at this field
    int offset_x[5];          // offset for track.png
    int offset_y[5];
};

#endif // __TRACK_H__
