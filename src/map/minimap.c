/*
 * $Id$
 */
/*
 *	Initial Release was taken from the FreeCraft Engine
 *	( http://freecraft.org )
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "game.h"
#include "log.h"
#include "video.h"
#include "tileset.h"
#include "map.h"
#include "minimap.h"
#include "ui.h"

sGraphic* MinimapGraphic;		/// generated minimap
int Minimap2MapX[MINIMAP_W];	/// fast conversion table
int Minimap2MapY[MINIMAP_H];	/// fast conversion table
int Map2MinimapX[MaxMapWidth];	/// fast conversion table
int Map2MinimapY[MaxMapHeight];	/// fast conversion table

int MinimapScale;		/// Minimap scale to fit into window
int MinimapX;			/// Minimap drawing position x offset
int MinimapY;			/// Minimap drawing position y offset

int MinimapWithTerrain=1;	/// display minimap with terrain
int MinimapFriendly=1;		/// switch colors of friendly units
int MinimapShowSelected=1;	/// highlight selected units

/**
**	Update minimap at map position x,y. This is called when the tile image
**	of a tile changes.
**
**	@param tx	Tile X position, where the map changed.
**	@param ty	Tile Y position, where the map changed.
**
**	FIXME: this can surely speeded up??
*/
void UpdateMinimapXY(int tx,int ty)
{
    int mx;
    int my;
    int x;
    int y;
    int scale;

    if( !(scale=(MinimapScale/MINIMAP_FAC)) ) {
	scale=1;
    }
    //
    //	Pixel 7,6 7,14, 15,6 15,14 are taken for the minimap picture.
    //
    ty*=TheMap.Info->Width;
    for( my=0; my<MINIMAP_H; my++ ) {
	y=Minimap2MapY[my];
	if( y<ty ) {
	    continue;
	}
	if( y>ty ) {
	    break;
	}

	for( mx=0; mx<MINIMAP_W; mx++ ) {
	    int tile;

	    x=Minimap2MapX[mx];
	    if( x<tx ) {
		continue;
	    }
	    if( x>tx ) {
		break;
	    }

	    tile=TheMap.Fields[x+y].Tile;
	    ((char*)MinimapGraphic->Frames)[mx+my*MINIMAP_W]=
		TheMap.Tiles[tile][7+(mx%scale)*8+(6+(my%scale)*8)*TileSizeX];
	}
    }
}

/**
**	Update a mini-map from the tiles of the map.
**
**	FIXME: this can surely speeded up??
*/
void UpdateMinimap(void)
{
    int mx;
    int my;
    int scale;

    if( !(scale=(MinimapScale/MINIMAP_FAC)) ) {
	scale=1;
    }

    //
    //	Pixel 7,6 7,14, 15,6 15,14 are taken for the minimap picture.
    //
    for( my=0; my<MINIMAP_H; my++ ) {
	for( mx=0; mx<MINIMAP_W; mx++ ) {
	    int tile;

	    tile=TheMap.Fields[Minimap2MapX[mx]+Minimap2MapY[my]].Tile;
	    ((char*)MinimapGraphic->Frames)[mx+my*MINIMAP_W]=
		TheMap.Tiles[tile][7+(mx%scale)*8+(6+(my%scale)*8)*TileSizeX];
	}
    }
}

/**
**	Create a mini-map from the tiles of the map.
**
**	@todo 	Scaling and scrolling the minmap is currently not supported.
*/
void CreateMinimap(void)
{
    int n;

    if( TheMap.Info->Width>TheMap.Info->Height ) {	// Scale too biggest value.
	n=TheMap.Info->Width;
    } else {
	n=TheMap.Info->Height;
    }
    MinimapScale=(MINIMAP_W*MINIMAP_FAC)/n;

    // FIXME: X,Y offset not supported!!
    MinimapX=0;
    MinimapY=0;

    //
    //	Calculate minimap fast lookup tables.
    //
    for( n=0; n<MINIMAP_W; ++n ) {
	Minimap2MapX[n]=(n*MINIMAP_FAC)/MinimapScale;
    }
    for( n=0; n<MINIMAP_H; ++n ) {
	Minimap2MapY[n]=((n*MINIMAP_FAC)/MinimapScale)*TheMap.Info->Width;
    }
    for( n=0; n<TheMap.Info->Width; ++n ) {
	Map2MinimapX[n]=(n*MinimapScale)/MINIMAP_FAC;
    }
    for( n=0; n<TheMap.Info->Height; ++n ) {
	Map2MinimapY[n]=(n*MinimapScale)/MINIMAP_FAC;
    }

    MinimapGraphic=NewGraphic(8,MINIMAP_W,MINIMAP_H);

    UpdateMinimap();
}

/**
**	Destroy mini-map.
*/
void DestroyMinimap(void)
{
    VideoSaveFree(MinimapGraphic);
    MinimapGraphic=NULL;
}

/**
**	Draw the mini-map with current viewpoint.
**
**	@param vx	View point X position.
**	@param vy	View point Y position.
**
**	@note This one of the hot-points in the program optimize and optimize!
*/
void DrawMinimap(int vx,int vy)
{
    static int RedPhase;
    int mx;
    int my;
    int x;
    int y;
    int w;
    int h;
    int h0;

    RedPhase^=1;

    x=TheUI.MinimapX+24;
    y=TheUI.MinimapY+2;

    //
    //	Draw the mini-map background.	Note draws a little too much.
    //
    VideoDrawSub(TheUI.Minimap.Graphic,24,2
	    ,TheUI.Minimap.Graphic->Width-48,TheUI.Minimap.Graphic->Height-4
	    ,x,y);

    //
    //	Draw the terrain
    //
    if( MinimapWithTerrain ) {
	for( my=0; my<MINIMAP_H; ++my ) {
	    for( mx=0; mx<MINIMAP_W; ++mx ) {
		VideoDrawPixel(((char*)MinimapGraphic->Frames)
		    [mx+my*MINIMAP_W],x+mx,y+my);
	    }
	}
    }

}

int OldMinimapCursorX;		/// Save MinimapCursorX
int OldMinimapCursorY;		/// Save MinimapCursorY
int OldMinimapCursorW;		/// Save MinimapCursorW
int OldMinimapCursorH;		/// Save MinimapCursorH
int OldMinimapCursorSize;	/// Saved image size

void* OldMinimapCursorImage;	/// Saved image behind cursor

/**
**	Hide minimap cursor.
*/
void HideMinimapCursor(void)
{
    int i;
    int w;
    int h;

    if( !OldMinimapCursorImage ) {
	return;
    }

    w=OldMinimapCursorW;
    h=OldMinimapCursorH;

    // FIXME: Attention 8/16/32 bpp!
    switch( VideoBpp ) {
    case 8:
	{ VMemType8* sp;
	  VMemType8* dp;
	sp=OldMinimapCursorImage;
	dp=VideoMemory8+OldMinimapCursorY*VideoWidth+OldMinimapCursorX;
	memcpy(dp,sp,w*sizeof(VMemType8));
	sp+=w;
	for( i=0; i<h; ++i ) {
	    *dp=*sp++;
	    dp[w]=*sp++;
	    dp+=VideoWidth;
	}
	memcpy(dp,sp,(w+1)*sizeof(VMemType8)); }
	break;
    case 15:
    case 16:
	{ VMemType16* sp;
	  VMemType16* dp;
	sp=OldMinimapCursorImage;
	dp=VideoMemory16+OldMinimapCursorY*VideoWidth+OldMinimapCursorX;
	memcpy(dp,sp,w*sizeof(VMemType16));
	sp+=w;
	for( i=0; i<h; ++i ) {
	    *dp=*sp++;
	    dp[w]=*sp++;
	    dp+=VideoWidth;
	}
	memcpy(dp,sp,(w+1)*sizeof(VMemType16)); }
	break;
    case 24:
	{ VMemType24* sp;
	  VMemType24* dp;
	sp=OldMinimapCursorImage;
	dp=VideoMemory24+OldMinimapCursorY*VideoWidth+OldMinimapCursorX;
	memcpy(dp,sp,w*sizeof(VMemType24));
	sp+=w;
	for( i=0; i<h; ++i ) {
	    *dp=*sp++;
	    dp[w]=*sp++;
	    dp+=VideoWidth;
	}
	memcpy(dp,sp,(w+1)*sizeof(VMemType24)); }
	break;
    case 32:
	{ VMemType32* sp;
	  VMemType32* dp;
	sp=OldMinimapCursorImage;
	dp=VideoMemory32+OldMinimapCursorY*VideoWidth+OldMinimapCursorX;
	memcpy(dp,sp,w*sizeof(VMemType32));
	sp+=w;
	for( i=0; i<h; ++i ) {
	    *dp=*sp++;
	    dp[w]=*sp++;
	    dp+=VideoWidth;
	}
	memcpy(dp,sp,(w+1)*sizeof(VMemType32)); }
	break;
    }
}

/**
**	Draw minimap cursor.
**
**	@param vx	View point X position.
**	@param vy	View point Y position.
*/
void DrawMinimapCursor(int vx,int vy)
{
    int x;
    int y;
    int w;
    int h;
    int i;

    OldMinimapCursorX=x=TheUI.MinimapX+24+(vx*MinimapScale)/MINIMAP_FAC;
    OldMinimapCursorY=y=TheUI.MinimapY+2+(vy*MinimapScale)/MINIMAP_FAC;
    OldMinimapCursorW=w=(MapWidth*MinimapScale)/MINIMAP_FAC-1;
    OldMinimapCursorH=h=(MapHeight*MinimapScale)/MINIMAP_FAC-1;

    switch( VideoBpp ) {
	case 8:
	    i=(w+1+h)*2*sizeof(VMemType8);
	    break;
	case 15:
	case 16:
	    i=(w+1+h)*2*sizeof(VMemType16);
	    break;
	case 24:
	    i=(w+1+h)*2*sizeof(VMemType24);
	    break;
	default:
	case 32:
	    i=(w+1+h)*2*sizeof(VMemType32);
	    break;
    }
    if( OldMinimapCursorSize<i ) {
	if( OldMinimapCursorImage ) {
	    OldMinimapCursorImage=realloc(OldMinimapCursorImage,i);
	} else {
	    OldMinimapCursorImage=malloc(i);
	}
	DebugLevel3("Cursor memory %d\n",i);
	OldMinimapCursorSize=i;
    }

    // FIXME: not 100% correct
    switch( VideoBpp ) {
    case 8:
	{ VMemType8* sp;
	VMemType8* dp;
	dp=OldMinimapCursorImage;
	sp=VideoMemory8+OldMinimapCursorY*VideoWidth+OldMinimapCursorX;
	memcpy(dp,sp,w*sizeof(VMemType8));
	dp+=w;
	for( i=0; i<h; ++i ) {
	    *dp++=*sp;
	    *dp++=sp[w];
	    sp+=VideoWidth;
	}
	memcpy(dp,sp,(w+1)*sizeof(VMemType8));
	break; }
    case 15:
    case 16:
	{ VMemType16* sp;
	VMemType16* dp;
	dp=OldMinimapCursorImage;
	sp=VideoMemory16+OldMinimapCursorY*VideoWidth+OldMinimapCursorX;
	memcpy(dp,sp,w*sizeof(VMemType16));
	dp+=w;
	for( i=0; i<h; ++i ) {
	    *dp++=*sp;
	    *dp++=sp[w];
	    sp+=VideoWidth;
	}
	memcpy(dp,sp,(w+1)*sizeof(VMemType16));
	break; }
    case 24:
	{ VMemType24* sp;
	VMemType24* dp;
	dp=OldMinimapCursorImage;
	sp=VideoMemory24+OldMinimapCursorY*VideoWidth+OldMinimapCursorX;
	memcpy(dp,sp,w*sizeof(VMemType24));
	dp+=w;
	for( i=0; i<h; ++i ) {
	    *dp++=*sp;
	    *dp++=sp[w];
	    sp+=VideoWidth;
	}
	memcpy(dp,sp,(w+1)*sizeof(VMemType24));
	break;
	}
    case 32:
	{ VMemType32* sp;
	VMemType32* dp;
	dp=OldMinimapCursorImage;
	sp=VideoMemory32+OldMinimapCursorY*VideoWidth+OldMinimapCursorX;
	memcpy(dp,sp,w*sizeof(VMemType32));
	dp+=w;
	for( i=0; i<h; ++i ) {
	    *dp++=*sp;
	    *dp++=sp[w];
	    sp+=VideoWidth;
	}
	memcpy(dp,sp,(w+1)*sizeof(VMemType32));
	break; }
    }
    VideoDrawRectangleClip(ColorWhite,x,y,w,h);
}