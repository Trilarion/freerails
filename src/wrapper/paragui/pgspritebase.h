#ifndef PG_SPRITEBASE_H
#define PG_SPRITEBASE_H

#include <SDL.h>
#include <SDL_image.h>
#include <map>
#include <string>

class PG_SpriteFrame
{
  public:
    PG_SpriteFrame(SDL_Surface* _image, int _pause) {image=_image; pause=_pause; };
    SDL_Surface *image;
    int pause;
};

class PG_SpriteBase
{
  public:
    void AddSprite(std::string index, std::string filename, int pause);
    int LoadDirectory(std::string dirname);

    PG_SpriteFrame* GetSprite(std::string index);
    PG_SpriteFrame* GetSprite();
    PG_SpriteFrame* GetNextSprite();
    int GetWidth() { return my_width; };
    int GetHeight() { return my_height; };

  private:
    std::map<std::string, PG_SpriteFrame*> my_sprites;
    std::map<std::string, PG_SpriteFrame*>::iterator my_sprites_it;
    int my_width, my_height;
};

#endif // PG_SPRITEBASE_H
