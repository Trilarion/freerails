#include "pgframeapplication.h"
#include <cassert>

PG_FrameHandler*  PG_FrameApplication::my_framehandler=NULL;
PG_NetHandler*  PG_FrameApplication::my_nethandler=NULL;
PG_Label*  PG_FrameApplication::my_fpslabel=NULL;

PG_FrameApplication::PG_FrameApplication()
{
}

PG_FrameApplication::~PG_FrameApplication() {
}

void PG_FrameApplication::SetFrameHandler(PG_FrameHandler* framehandler) {
  my_framehandler=framehandler;
}

PG_FrameHandler* PG_FrameApplication::GetFrameHandler() {
  return my_framehandler;
}

void PG_FrameApplication::SetNetHandler(PG_NetHandler* nethandler) {
  my_nethandler=nethandler;
}

PG_NetHandler* PG_FrameApplication::GetNetHandler() {
  return my_nethandler;
}

void PG_FrameApplication::SetFPSLabel(PG_Label* fpslabel) {
  my_fpslabel=fpslabel;
}

PG_Label* PG_FrameApplication::GetFPSLabel() {
  return my_fpslabel;
}

/**  */
SDL_Thread* PG_FrameApplication::Run(bool threaded) {
#ifndef WIN32
	if(threaded) {
		SDL_Thread* thrd = SDL_CreateThread(PG_Application::RunEventLoop, this);
		return thrd;
	}
#endif
	RunEventLoop(this);
	return NULL;
}

/** Event processing loop */
int PG_FrameApplication::RunEventLoop(void* data) {
	PG_FrameApplication* object = static_cast<PG_FrameApplication*>(data);
	SDL_Event event;
	Uint32 then, now, frames;
	SDL_Surface *screen = PG_Application::GetScreen();
	DisableDirtyUpdates(true);
	SetBulkMode();
	
	my_quitEventLoop = false;
	assert(data);

	FlushEventQueue();

	frames = 0;
	then = SDL_GetTicks();
	bool processed=false;
	while(!my_quitEventLoop) {
		
                my_nethandler->checkNet();
		if (SDL_PollEvent(&event)) {
		  processed = object->PumpIntoEventQueue(&event);
		}
		++frames;
		now = SDL_GetTicks();
		if ( now > then+1000 ) {
		                if (my_fpslabel!=NULL)
				{
				  my_fpslabel->SetTextFormat("%3.2f FPS", ((double)frames*1000)/(now-then));
				}
				if((now-then) > 1000) {
					then = now;
					frames=0;
				}
		}
                my_framehandler->DrawBackground(screen);
		my_framehandler->NextFrame(screen);
		PG_Widget::BulkBlit();
	        SDL_Flip(screen);
	}
	return -1;
}

bool PG_FrameApplication::eventQuit(int id, PG_MessageObject* widget, unsigned long data) {

    SetBulkMode(false);
    PG_Application::eventQuit(id, widget, data);
}
