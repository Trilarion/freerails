#include <vector>

#include "pgframehandler.h"

PG_FrameHandler::PG_FrameHandler(PG_FrameApplication* app)
{
  my_app=app;
  my_appsurface=app->GetScreen();
  mouse_over=true;
}

PG_FrameHandler::~PG_FrameHandler()
{
}

void PG_FrameHandler::SetBackgroundColor(Uint32 background)
{
  my_background=background;
}

Uint32 PG_FrameHandler::GetBackgroundColor()
{
  return my_background;
}

void PG_FrameHandler::AddFrameObject(PG_FrameObject* object)
{
  my_frameobjects.push_back(object);
}

bool PG_FrameHandler::RemoveFrameObject(PG_FrameObject* object)
{
  vector<PG_FrameObject*>::iterator it;
  for (it=my_frameobjects.begin(); it!=my_frameobjects.end(); ++it)
  {
    if (object==*it)
    {
      my_frameobjects.erase(it);
      return true;
    }
  }
  return false;
}

bool PG_FrameHandler::DeleteFrameObject(PG_FrameObject* object)
{
  if (RemoveFrameObject(object))
  {
    delete object;
    return true;
  }
  return false;
}
