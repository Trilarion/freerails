#ifndef PG_FRAMEOBJECT_H
#define PG_FRAMEOBJECT_H

#ifdef SWIG
%include "swigcommon.h"
%module pgframeobject
%{
#include "pgframeobject.h"
%}
#endif

#include "pgmessageobject.h"

#include <SDL.h>

class DECLSPEC PG_FrameObject : public PG_MessageObject {
public:
	/**
	Creates a PG_FrameObject wich have no surfaces
	*/
	PG_FrameObject();

	/**
	Destroys a PG_FrameObject
	*/
	virtual ~PG_FrameObject();

	/**
	The NextFrame function is called from PG_FrameHandler
	*/
	virtual void NextFrame(SDL_Surface* surface, Uint32 background);

protected:
        SDL_Rect* pos;
	SDL_Surface* sprite;
};

#endif // PG_FRAMEOBJECT_H
