
#ifndef PG_FRAMEAPPLICATION_H
#define PG_FRAMEAPPLICATION_H

#ifdef SWIG
%include "swigcommon.h"
%module pggameapplication
%{
#include "pgframeapplication.h"
%}
#endif

#include "pgapplication.h"
#include "pgframehandler.h"
#include "pglabel.h"

#include <SDL.h>

class PG_FrameHandler;

class PG_FrameApplication : public PG_Application  {
public:

        PG_FrameApplication();
	~PG_FrameApplication();

	/**
	Run the applications main eventloop

	@param	threaded	run the eventloop in a separate thread
	@return			pointer to event thread
	If theaded is false this function will exit when the eventloop quits (MSG_QUIT). If threaded is true
	it will return immediately and a thread processing events is started.
	CAUTION: Threaded eventloops are unsuported under Win32 (windows specific behavior)
	*/
	SDL_Thread* Run(bool threaded = false);

        /**
	Run the modal message pump. This function will exit when the main window was closed.
	Every Time it calls the NextFrame function of the PG_FrameHandler Class.
	*/
	static int RunEventLoop(void* data);
	
	static void SetFrameHandler(PG_FrameHandler* framehandler);
	static PG_FrameHandler* GetFrameHandler();

	static void SetFPSLabel(PG_Label* fpslabel);
	static PG_Label* GetFPSLabel();

private:
        static PG_FrameHandler* my_framehandler;
        static PG_Label* my_fpslabel;
	
	PG_FrameApplication(const PG_FrameApplication&);
	PG_FrameApplication& operator=(const PG_FrameApplication&);
};
#endif // PG_FRAMEAPPLICATION_H
