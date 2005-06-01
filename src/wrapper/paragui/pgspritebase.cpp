#include "pgspritebase.h"
#include <string>
#include <iostream>

void PG_SpriteBase::AddSprite(std::string index, std::string filename, int pause)
{
  SDL_Surface* tmp_sur= IMG_Load(filename.c_str());
  if (!tmp_sur)
  {
    std::cerr << "Error on loading image: " << filename << "\n" << IMG_GetError() << std::endl;
    return;
  }
  PG_SpriteFrame* tmp=new PG_SpriteFrame(tmp_sur,pause);
  my_sprites[index]=tmp;
  my_sprites_it = my_sprites.begin();
}

PG_SpriteFrame* PG_SpriteBase::GetSprite(std::string index)
{
  std::map<std::string, PG_SpriteFrame*>::iterator temp_it;
  temp_it = my_sprites.find(index);
  if (temp_it != my_sprites.end())
  {
    my_sprites_it = temp_it;
    return (*my_sprites_it).second;
  } else
  {
    my_sprites_it = my_sprites.begin();
    return NULL;
  }
}

PG_SpriteFrame* PG_SpriteBase::GetSprite()
{
  return (*my_sprites_it).second;
}

PG_SpriteFrame* PG_SpriteBase::GetNextSprite()
{
  my_sprites_it++;
  if (my_sprites_it == my_sprites.end())
  {
    my_sprites_it = my_sprites.begin();
  }
  return (*my_sprites_it).second;
}

int PG_SpriteBase::LoadDirectory(std::string dirname)
{
  std::cerr << "Loading " << dirname << std::endl;
  std::string filename;
  char buffer[255];
  char name[255];
  char index[255];
  int pause=0;
  FILE *fp;

  filename = dirname+"/tiles.conf";

  if((fp=fopen(filename.c_str(), "r")) == NULL)
  {
    std::cerr << "ERROR opening file " << filename << std::endl;
    return -1;
  }

  while(!feof(fp))
  {
    fgets(buffer, 255, fp);
    if(buffer[0] != '#' && buffer[0] != '\r' && buffer[0] != '\0' && buffer[0] != '\n' && strlen(buffer) != 0)
    {
      sscanf(buffer, "%s %s %d", name, index, &pause);
      AddSprite(index, dirname+"/"+name, pause);
    }
  }
  fclose(fp);
  return 0;
}
