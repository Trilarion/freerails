#include "pgspriteobject.h"

int PG_SpriteObject::init(PG_SpriteBase *base)
{
  mSpriteBase = base;
  mBackreplacement = SDL_DisplayFormat(mSpriteBase->GetSprite()->image);
  mSpeed=0;
  mLastupdate=SDL_GetTicks();
  return 0;
}

void PG_SpriteObject::clearBG(SDL_Surface *screen)
{
  if(mDrawn==1)
  {
    SDL_Rect dest;
    dest.x = (int)mOldX;
    dest.y = (int)mOldY;
    dest.w = mSpriteBase->GetWidth();
    dest.h = mSpriteBase->GetHeight();
    SDL_BlitSurface(mBackreplacement, NULL, screen, &dest);
  }
}

void PG_SpriteObject::updateBG(SDL_Surface *screen)
{
  SDL_Rect srcrect;
  srcrect.x = (int)mX;
  srcrect.y = (int)mY;
  srcrect.w = mSpriteBase->GetWidth();
  srcrect.h = mSpriteBase->GetHeight();
  mOldX=mX;mOldY=mY;
  SDL_BlitSurface(screen, &srcrect, mBackreplacement, NULL);
}

void PG_SpriteObject::Draw(SDL_Surface *screen)
{
  if(mAnimating == 1)
  {
    if(mLastupdate+mSpriteBase->GetSprite()->pause*mSpeed<SDL_GetTicks())
    {
      mLastupdate += mSpriteBase->GetSprite()->pause*mSpeed;
      mSpriteBase->GetNextSprite();
    }
  }

  if(mDrawn==0) mDrawn=1;

  SDL_Rect dest;
  dest.x = (int)mX; dest.y = (int)mY;
  SDL_BlitSurface(mSpriteBase->GetSprite()->image, NULL, screen, &dest);
}
