## $Id$

TOPDIR=		../..
include $(TOPDIR)/Rules.make

OBJS = ../ai/ai.o


all : $(OBJS)
	
%.o: %.c
	$(CC) -c $(PARAM) $(INC) $< -o $@
	ar cru ../libgame.a $@
collect:
	ar cru ../libgame.a $(OBJS)