/** $Id$
  * Base class for ALL the elements in the game
  */
 
#ifndef __MAPFIELD_H__
#define __MAPFIELD_H__

#include "GameElement.h"
#include "Track.h"

class MapField {
public:
  enum FieldType { grass=0, wood, dessert, jungle, river, ocean, foothills, hills, mountain };

  /** Constructor */
  MapField(FieldType _type, unsigned short _cost, unsigned short _height);
  /** Destructor */
  virtual ~MapField();
  
  void setType(FieldType _type) { type=_type; };
  FieldType getType() { return type; };
  
  void setCost(unsigned short _cost) { cost=_cost; };
  unsigned short getCost() { return cost; };
  
  void setHeight(unsigned short _height) { height=_height; };
  unsigned short getHeight() { return height; };
  
  void setElement(GameElement* _element) { element=_element; };
  GameElement* getElement() { return element; };
  
  void setTrack(Track* _track) { track=_track; };
  Track* getTrack() { return track; };

private:
  FieldType type;
  unsigned short cost;
  unsigned short height;
  GameElement* element;
  Track* track;
};

#endif // __MAPFIELD_H__