if WRAPPER_QT
WRAPPERINC = $(QT_INCLUDES) -I$(top_srcdir)/src/wrapper/qt \
    -I$(top_srcdir)/src/network/qt
freerails_LDFLAGS = $(QT_LDFLAGS) -lqt
WRAPPERLIBS = ../wrapper/qt/libwrapper_qt.a \
    ../network/qt/libnetwork_qt.a
endif

if WRAPPER_PARAGUI
WRAPPERINC = $(SDL_CFLAGS) $(PARAGUI_CFLAGS) -I$(top_srcdir)/src/wrapper/paragui \
    -I$(top_srcdir)/src/network/libSDL_net
freerails_LDFLAGS = $(SDL_LIBS) $(PARAGUI_LIBS) -lSDL_ttf -lSDL_image -lSDL_net
WRAPPERLIBS = ../wrapper/paragui/libwrapper_paragui.a \
    ../network/libSDL_net/libnetwork_sdl.a
endif

INCLUDES = $(WRAPPERINC) -I$(srcdir) -I$(top_srcdir)/src/wrapper \
    -I$(top_srcdir) -I$(top_srcdir)/src/elements -I$(top_srcdir)/src/controllers

bin_PROGRAMS = freerails

freerails_SOURCES = i18n.cpp \
    main.cpp \
    game.cpp

freerails_DEPENDENCIES = ../wrapper/libwrapperbase.a ../elements/libelements.a \
    ../map/libmap.a ../controllers/libcontrollers.a $(WRAPPERLIBS)
freerails_LDADD = ../wrapper/libwrapperbase.a ../elements/libelements.a \
    ../map/libmap.a ../controllers/libcontrollers.a $(WRAPPERLIBS)
