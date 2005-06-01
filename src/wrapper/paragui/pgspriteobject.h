#ifndef PG_SPRITEOBJECT_H
#define PG_SPRITEOBJECT_H

#include <SDL.h>

#include "pgframeobject.h"
#include "pgspritebase.h"

class PG_SpriteObject : public PG_FrameObject
{
  public:
  PG_SpriteObject() {}
  ~PG_SpriteObject() {};
  int init(PG_SpriteBase *base);

  void Draw(SDL_Surface *surface);
  void NextFrame(SDL_Surface *surface, Uint32 background) { Draw(surface); };

  void clearBG(SDL_Surface *screen);
  void updateBG(SDL_Surface *screen);

  void setFrame(int nr) { mFrame = nr; }
  int getFrame() { return mFrame; }

  void setFrame(std::string index) { mSpriteBase->GetSprite(index); }

  void setSpeed(float nr) { mSpeed = nr; }
  float getSpeed() { return mSpeed; }

  void toggleAnim() { mAnimating = !mAnimating; }
  void startAnim() { mAnimating = 1; mLastupdate = SDL_GetTicks(); }
  void stopAnim() { mAnimating = 0; }
  void rewind() { mFrame = 0; mLastupdate = SDL_GetTicks(); }

  void xadd(float nr) { mX+=nr; }
  void yadd(float nr) { mY+=nr; }
  void xset(float nr) { mX=nr; }
  void yset(float nr) { mY=nr; }
  void set(float xx, float yy) { mX=xx; mY=yy; }

  float getx() {return mX;}
  float gety() {return mY;}
  
  int getw() {return mSpriteBase->GetWidth();}
  int geth() {return mSpriteBase->GetHeight();}

  private:
  int mFrame;
  float mX, mY, mOldX, mOldY;
  int mAnimating;
  int mDrawn;
  float mSpeed;
  float mLastupdate;
  PG_SpriteBase *mSpriteBase;
  SDL_Surface *mBackreplacement;
};

#endif // PG_SPRITEOBJECT
