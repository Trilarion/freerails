#ifndef PG_FRAMEHANDLER_H
#define PG_FRAMEHANDLER_H

#ifdef SWIG
%include "swigcommon.h"
%module pgframe
%{
#include "pgframehandler.h"
%}
#endif

#include "pgmessageobject.h"
#include "pgframeapplication.h"
#include "pgframeobject.h"

#include <vector>
#include <SDL.h>

class PG_FrameApplication;

class DECLSPEC PG_FrameHandler : public PG_MessageObject {
public:
	/**
	Creates a PG_FrameHandler wich have no surfaces
	*/
	PG_FrameHandler(PG_FrameApplication* app);

	/**
	Destroys a PG_FrameHandler
	*/
	virtual ~PG_FrameHandler();

	/**
	The NextFrame function is called from PG_FrameApplication
	*/
	virtual void NextFrame(){};
	
	void SetBackgroundColor(Uint32 background);
	Uint32 GetBackgroundColor();
	
	/**
	Add a new PG_FrameObject, that should be called on next Frame
	@param object Pointer to the PG_FrameObject that should be added
	*/
	void AddFrameObject(PG_FrameObject* object);

	/**
	Remove a PG_FrameObject from the list of calling objects
	@param object Pointer to the PG_FrameObject that should be removed
	@return true if a object have been removed
	*/
	bool RemoveFrameObject(PG_FrameObject* object);

	/**
	Remove a PG_FrameObject from the list of calling objects and
	Delete it from Memory.
	@param object Pointer to the PG_FrameObject that should be deleted
	@return true if a object have been deleted
	*/
	bool DeleteFrameObject(PG_FrameObject* object);

protected:
        vector<PG_FrameObject*> my_frameobjects;
	SDL_Surface* my_appsurface;
	PG_FrameApplication* my_app;
	Uint32 my_background;
};

#endif // PG_FRAMEHANDLER_H
