bin_PROGRAMS = freerails

if WRAPPER_QT
    WRAPPER_LDF = $(LIB_QT) 
endif

if WRAPPER_PARAGUI
    WRAPPER_LDF = $(SDL_LIBS) $(PARAGUI_LIBS) -lSDL_ttf -lSDL_image
endif

freerails_LDADD = ../wrapper/$(WRAPPER)/libwrapper.a ../game/libgame.a \
    ../map/libmap.a ../controllers/libcontrollers.a ../elements/libelements.a \
    ../wrapper/libwrapperbase.a ../network/libnetwork.a
freerails_LDFLAGS = $(WRAPPER_LDF) $(all_libraries)
INCLUDES = $(all_includes) -I$(top_srcdir)/src/wrapper/$(WRAPPER) \
    -I$(srcdir) -I$(top_srcdir)/src/wrapper \
    -I$(top_srcdir) -I$(top_srcdir)/src/map \
    -I$(top_srcdir)/src/elements -I$(top_srcdir)/src/controllers \
    -I$(top_srcdir)/src/network -I$(top_srcdir)/src/game

freerails_SOURCES = game.cpp \
    i18n.cpp \
    main.cpp
