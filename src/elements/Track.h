/** @short Class that represents a track
  *
  * @author Alexander Opitz <opitz@primacom.net>
  * @version $Id$
  */

#ifndef __TRACK_H__
#define __TRACK_H__

#include "GameElement.h"
#include "GamePosElement.h"

#define TrackGoNorth            0x00000080
#define TrackGoNorthEast        0x00000040
#define TrackGoEast             0x00000020
#define TrackGoSouthEast        0x00000010
#define TrackGoSouth            0x00000008
#define TrackGoSouthWest        0x00000004
#define TrackGoWest             0x00000002
#define TrackGoNorthWest        0x00000001

#define TrackIsBlocked          0x00000100  //Means there is a Station, Signal, Bridge etc.
					    //which blocks to add more directions.

class Track : public GamePosElement
{
  public:
    /** Constructs a track */
    Track(unsigned int _posX, unsigned int _posY, Player* _player, unsigned int _connect);
    ~Track();
    
    /** Serialization */
    void serialize(Serializer* _serializer);
    void deserialize(Serializer* _serializer);
    
    // get connection
    unsigned int getConnect() {return connect;};
    // sets the connection
    void setConnect(unsigned int _connect) {connect = _connect;};

  private:
    unsigned int connect;   // connection at this field
};

#endif // __TRACK_H__
