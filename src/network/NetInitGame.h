#ifndef __NETINITGAME_H__
#define __NETINITGAME_H__

#include "Serializeable.h"

/*************************************************************/
/*                  PacketRequestJoinGame                    */
/*************************************************************/

class PacketRequestJoinGame : public Serializeable{
  /* MSG_TYPE == 1 */
 public:
  PacketRequestJoinGame();
  ~PacketRequestJoinGame();

  void serialize(Serializer* _serializer);
  void deserialize(Serializer* _serializer);

  void setName(std::string s){name=s;};
  std::string getName(){return name;};


 private:
  
  std::string name;
  
};


/*************************************************************/
/*                PacketReplyRequestJoinGame                 */
/*************************************************************/

class PacketReplyRequestJoinGame : public Serializeable{
  /* MSG_TYPE == 2 */
 public:
  PacketReplyRequestJoinGame();
  ~PacketReplyRequestJoinGame();
 
  void serialize(Serializer* _serializer);
  void deserialize(Serializer* _serializer);

  void setAnswer(short a){answer=a;};
  short getAnswer(){return answer;};

 private:
  short answer;
  
  
};


/*************************************************************/
/*                        PacketMapInfo                      */
/*************************************************************/



#endif /* __NETINITGAME_H__ */



