/*
 * $Id$
 */
/*
 *	Initial Release was taken from the FreeCraft Engine
 *	( http://freecraft.org )
 */

//FIXME: from opi ... can we use it or do wee need our own pathfinder?

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>

#include "game.h"
#include "log.h"
#include "video.h"
#include "tileset.h"
#include "map.h"
#include "sound_id.h"
#include "player.h"
#include "pathfinder.h"
#include "ui.h"

#ifndef MAX_PATH_LENGTH
#define MAX_PATH_LENGTH		9	/// Maximal path part returned.
#endif

#define USE_BEST			/// Goto best point, don't stop.

/*----------------------------------------------------------------------------
--	Variables
----------------------------------------------------------------------------*/

/**
**	The matrix is used to generated the paths.
**
**		0:	Nothing must check if usable.
**		1-8:	Field on the path 1->2->3->4->5...
**		88:	Marks the possible goal fields.
**		98:	Marks map border, for faster limits checks.
*/
unsigned char Matrix[(MaxMapWidth+2)*(MaxMapHeight+2)];	/// Path matrix

IfDebug(
unsigned PfCounterFail;
unsigned PfCounterOk;
unsigned PfCounterDepth;
unsigned PfCounterNotReachable;
);

/*----------------------------------------------------------------------------
--	Functions
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
--	PATH-FINDER LOW-LEVEL
----------------------------------------------------------------------------*/

/**
**	Create empty movement matrix.
**
**	NOTE: double border for ships/flyers.
**
**		98 98 98 98 98
**		98 98 98 98 98
**		98	    98
**		98	    98
**		98 98 98 98 98
*/
unsigned char* CreateMatrix(void)
{
    unsigned char* matrix;
    unsigned i;
    unsigned w;
    unsigned h;
    unsigned e;

    w=TheMap.Info->Width+2;
    h=TheMap.Info->Height;
    matrix=Matrix;
    //matrix=malloc(w*(h+2));

    i=w+w+1;
    memset(matrix,98,i);		// +1 for ships!
    memset(matrix+i,0,w*h);		// initialize matrix

    for( e=i+w*h; i<e; ) {		// mark left and right border
	matrix[i]=98;
	i+=w;
	matrix[i-1]=98;
    }
    memset(matrix+i,98,w+1);		// +1 for ships!

    return matrix;
}

/**
**	Mark place in matrix.
**
**	@param x	X position of target area
**	@param y	Y position of target area
**	@param w	Width of target area
**	@param h	Height of target area
**	@param matrix	Target area marked in matrix
*/
void MarkPlaceInMatrix(int x,int y,int w,int h,unsigned char* matrix)
{
    int xi;
    int xe;
    int mw;

    if( x<0 ) {				// reduce to map limits
	w-=x;
	x=0;
    }
    if( x+w>TheMap.Info->Width ) {
	w=TheMap.Info->Width-x;
    }
    if( y<0 ) {
	h-=y;
	y=0;
    }
    if( y+h>TheMap.Info->Height ) {
	h=TheMap.Info->Height-y;
    }

    DebugCheck( h==0 || w==0 );		// atleast one tile should be there!

    mw=TheMap.Info->Width+2;
    matrix+=mw+mw+2;
    xe=x+w;
    while( h-- ) {			// mark the rectangle
	for( xi=x; xi<xe; ++xi ) {
	    matrix[xi+y*mw]=88;
	}
	++y;
    }
}

/**
**	Trace path in the matrix back to origin.
**
**	@param matrix	Matrix (not real start!)
**	@param add	Step value.
**	@param x	X start point of trace back.
**	@param y	Y start point of trace back.
**	@param depth	Depth of path and marks start value (1..8)
**	@param path	OUT: here are the path directions stored.
*/
void PathTraceBack(const unsigned char* matrix,int add,int x,int y
	,int depth,unsigned char* path)
{
    int w;
    int w2;
    const unsigned char* m;
    int d;
    int n;

    w=TheMap.Info->Width+2;
    m=matrix+x+y*w;			// End of path in matrix.
    w*=add;
    w2=w+w;

    //
    //	Find the way back, nodes are numbered ascending.
    //
    for( ;; ) {
	--depth;
	n=(depth&0x7)+1;

	//
	//	Directions stored in path:
	//		7 0 1
	//		6 . 2
	//		5 4 3
	//
	d=6;
	m+=add;				// +1, 0
	if( *m!=n ) {			// test all neighbors
	  d=2;
	  m-=add<<1;			// -1, 0
	  if( *m!=n ) {
	    d=0;
	    m+=add+w;			//  0,+1
	    if( *m!=n ) {
	      d=4;
	      m-=w2;			//  0,-1
	      if( *m!=n ) {
		d=5;
		m+=add;			// +1,-1
		if( *m!=n ) {
		  d=3;
		  m-=add<<1;		// -1,-1
		  if( *m!=n ) {
		    d=1;
		    m+=w2;		// -1,+1
		    if( *m!=n ) {
		      d=7;
		      m+=add<<1;	// +1,+1
		      if( *m!=n ) {
			  return;
		      }
		    }
		  }
		}
	      }
	    }
	  }
	}
	// found: continue
	if( depth<MAX_PATH_LENGTH ) {
	    path[depth]=d;
	}
    }
}