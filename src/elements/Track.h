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

#define TrackGoNorth            0x00000001
#define TrackGoNorthEast        0x00000002
#define TrackGoEast             0x00000004
#define TrackGoSouthEast        0x00000008
#define TrackGoSouth            0x00000010
#define TrackGoSouthWest        0x00000020
#define TrackGoWest             0x00000040
#define TrackGoNorthWest        0x00000080

#define TrackIsStationDepot     0x00000100
#define TrackIsStationStation   0x00000200
#define TrackIsStationTerminal  0x00000400
#define TrackIsStation          0x00000700

#define TrackIsBridgeWood       0x00010000
#define TrackIsBridgeSteel      0x00020000
#define TrackIsBridgeStone      0x00040000
#define TrackIsBridge           0x00070000

#define TrackIsTunnel           0x00100000
#define TrackIsSignal           0x00200000

#define TrackIsDouble           0x01000000

#define TrackHasCornerNorthEast 0x10000000
#define TrackHasCornerSouthEast 0x20000000
#define TrackHasCornerSouthWest 0x40000000
#define TrackHasCornerNorthWest 0x80000000

class Track : public GameElement
{
  public:
    /** Constructs a track */
    Track(GameController* _controller, Player* _player);
    ~Track();

    unsigned int getConnect() { return connect; };
    void setConnect(unsigned int _con);

    void getTrackTile(int i, int *x, int *y);

  private:
    GameController *controller;
                           
    unsigned int connect;   // connection at this field
    int offset_x[5];        // offset for track.png
    int offset_y[5];
};

#endif // __TRACK_H__
