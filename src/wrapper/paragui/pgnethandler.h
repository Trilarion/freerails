#ifndef PG_NETHANDLER_H
#define PG_NETHANDLER_H

#ifdef SWIG
%include "swigcommon.h"
%module pgframe
%{
#include "pgnethandler.h"
%}
#endif

#include "pgmessageobject.h"
#include "pgframeapplication.h"

#include <vector>
#include <SDL.h>

class PG_FrameApplication;

class DECLSPEC PG_NetHandler : public PG_MessageObject {
public:
	/**
	Creates a PG_NetHandler wich have no surfaces
	*/
	PG_NetHandler(PG_FrameApplication* app);

	/**
	Destroys a PG_NetHandler
	*/
	virtual ~PG_NetHandler();

	/**
	The checkNet function is called from PG_FrameApplication
	*/
	virtual void checkNet(){};
	PG_FrameApplication* my_app;
};

#endif // PG_NETHANDLER_H
