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

#include "game.h"
#include "log.h"
#include "player.h"

#include "pathfinder.h"

/*----------------------------------------------------------------------------
--	Declarations
----------------------------------------------------------------------------*/

typedef struct _node_ {
    short	Direction;	/// Direction for trace back
    short       InGoal;         /// is this point in the goal
    int		CostFromStart;	/// Real costs to reach this point
} Node;

typedef struct _open_ {
    int		X;		/// X coordinate
    int		Y;		/// Y coordinate
    int		O;		/// Offset into matrix
    int		Costs;		/// complete costs to goal
} Open;

/// heuristic cost fonction for a star
#define AStarCosts(sx,sy,ex,ey)	max(abs(sx-ex),abs(sy-ey))

/*----------------------------------------------------------------------------
--	Variables
----------------------------------------------------------------------------*/

/// cost matrix
Node *AStarMatrix;
/// a list of close nodes, helps to speed up the matrix cleaning
int *CloseSet;
int Threshold;
int OpenSetMaxSize;
int AStarMatrixSize;
#define MAX_CLOSE_SET_RATIO 4
#define MAX_OPEN_SET_RATIO 8	// 10,16 to small

/// see pathfinder.h
int AStarFixedUnitCrossingCost=MaxMapWidth*MaxMapHeight;
int AStarMovingUnitCrossingCost=2;
int AStarOn=0;

/**
 ** The Open set is handled by a Heap stored in a table
 ** 0 is the root
 ** node i left son is at 2*i+1 and right son is at 2*i+2
 */

/// The set of Open nodes
Open *OpenSet;
/// The size of the open node set
int OpenSetSize;

/**
 ** Init A* data structures
 */
void InitAStar(void)
{
    if(AStarOn) {
	if(!AStarMatrix) {
	    AStarMatrixSize=sizeof(Node)*TheMap.Info->Width*TheMap.Info->Height;
	    AStarMatrix=(Node *)malloc(AStarMatrixSize);
	    Threshold=TheMap.Info->Width*TheMap.Info->Height/MAX_CLOSE_SET_RATIO;
	    CloseSet=(int *)malloc(sizeof(int)*Threshold);
	    OpenSetMaxSize=TheMap.Info->Width*TheMap.Info->Height/MAX_OPEN_SET_RATIO;
	    OpenSet=(Open *)malloc(sizeof(Open)*OpenSetMaxSize);
	}
    }
}

/**
 ** Free A* data structure
 */
void FreeAStar(void)
{
    if(AStarMatrix) {
	free(AStarMatrix);
	AStarMatrix=NULL;
	free(CloseSet);
	free(OpenSet);
    }
}

/**
**	Prepare path finder.
*/
void AStarPrepare(void)
{
    memset(AStarMatrix,0,AStarMatrixSize);
}

/**
 ** Clean up the AStarMatrix
 */
void AStarCleanUp(int num_in_close)
{
    int i;
    if(num_in_close>=Threshold) {
	AStarPrepare();
    } else {
	for(i=0;i<num_in_close;i++) {
	  AStarMatrix[CloseSet[i]].CostFromStart=0;
	  AStarMatrix[CloseSet[i]].InGoal=0;
	}
    }
}

/**
 ** Find the best node in the current open node set
 ** Returns the position of this node in the open node set (always 0 in the
 ** current heap based implementation)
 */
int AStarFindMinimum()
{
    return 0;
}

/**
 ** Remove the minimum from the open node set (and update the heap)
 ** pos is the position of the minimum (0 in the heap based implementation)
 */
void AStarRemoveMinimum(int pos)
{
    int i,j,end;
    Open swap;
    if(--OpenSetSize) {
	OpenSet[pos]=OpenSet[OpenSetSize];
	// now we exchange the new root with its smallest child until the
	// order is correct
	i=0;
	end=(OpenSetSize>>1)-1;
	while(i<=end) {
	    j=(i<<1)+1;
	    if(j<OpenSetSize-1 && OpenSet[j].Costs>=OpenSet[j+1].Costs)
		j++;
	    if(OpenSet[i].Costs>OpenSet[j].Costs) {
		swap=OpenSet[i];
		OpenSet[i]=OpenSet[j];
		OpenSet[j]=swap;
		i=j;
	    } else {
		break;
	    }
	}
    }
}

/**
 ** Add a new node to the open set (and update the heap structure)
 */
void AStarAddNode(int x,int y,int o,int costs)
{
    int i=OpenSetSize;
    int j;
    Open swap;

    if(OpenSetSize>=OpenSetMaxSize) {
	fprintf(stderr, "A* internal error: raise Open Set Max Size "
		"(current value %d)\n",OpenSetMaxSize);
	Exit(-1);
    }
    OpenSet[i].X=x;
    OpenSet[i].Y=y;
    OpenSet[i].O=o;
    OpenSet[i].Costs=costs;
    OpenSetSize++;
    while(i>0) {
	j=(i-1)>>1;
	if(OpenSet[i].Costs<OpenSet[j].Costs) {
	    swap=OpenSet[i];
	    OpenSet[i]=OpenSet[j];
	    OpenSet[j]=swap;
	    i=j;
	} else {
	    break;
	}
    }
}

/**
 ** Change the cost associated to an open node. The new cost MUST BE LOWER
 ** than the old one in the current heap based implementation.
 */
void AStarReplaceNode(int pos,int costs)
{
    int i=pos;
    int j;
    Open swap;
    OpenSet[pos].Costs=costs;
    // we need to go up, as the cost can only decrease
    while(i>0) {
	j=(i-1)>>1;
	if(OpenSet[i].Costs<OpenSet[j].Costs) {
	    swap=OpenSet[i];
	    OpenSet[i]=OpenSet[j];
	    OpenSet[j]=swap;
	    i=j;
	} else {
	    break;
	}
    }
}


/**
 ** Check if a node is already in the open set.
 ** Return -1 if not found and the position of the node in the table if found.
 */
int AStarFindNode(int eo)
{
    int i;
    for( i=0; i<OpenSetSize; ++i ) {
	if( OpenSet[i].O==eo ) {
	    return i;
	}
    }
    return -1;
}

/**
 ** Compute the cost of crossing tile (dx,dy)
 ** -1 -> impossible to cross
 **  0 -> no induced cost, except move
 ** >0 -> costly tile
 */
int CostMoveTo(int ex,int ey,int mask,int current_cost) {
    return 0;
}