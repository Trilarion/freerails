bin_PROGRAMS = freerails

if WRAPPER_QT
WRAPPERINC = $(QT_INCLUDES) -I$(top_srcdir)/src/wrapper/qt
freerails_LDFLAGS = $(QT_LDFLAGS) -lqt
freerails_DEPENDENCIES = ../wrapper/libwrapperbase.a ../elements/libelements.a \
    ../map/libmap.a ../controllers/libcontrollers.a ../wrapper/qt/libwrapper_qt.a \
    ../network/libnetwork.a
freerails_LDADD = ../wrapper/qt/libwrapper_qt.a ../elements/libelements.a \
    ../map/libmap.a ../controllers/libcontrollers.a  ../wrapper/libwrapperbase.a\
    ../network/libnetwork.a
endif

if WRAPPER_PARAGUI
WRAPPERINC = $(SDL_CFLAGS) $(PARAGUI_CFLAGS) -I$(top_srcdir)/src/wrapper/paragui
freerails_LDFLAGS = $(SDL_LIBS) $(PARAGUI_LIBS) -lSDL_ttf -lSDL_image -lSDL_net
freerails_DEPENDENCIES = ../wrapper/libwrapperbase.a ../elements/libelements.a \
    ../map/libmap.a ../controllers/libcontrollers.a ../wrapper/paragui/libwrapper_paragui.a \
    ../network/libnetwork.a
freerails_LDADD = ../wrapper/libwrapperbase.a ../elements/libelements.a \
    ../map/libmap.a ../controllers/libcontrollers.a ../wrapper/paragui/libwrapper_paragui.a \
    ../network/libnetwork.a
endif

INCLUDES = $(WRAPPERINC) -I$(srcdir) -I$(top_srcdir)/src/wrapper \
    -I$(top_srcdir) -I$(top_srcdir)/src/map -I$(top_srcdir)/src/elements -I$(top_srcdir)/src/controllers -I$(top_srcdir)/src/network -I$(top_srcdir)/src/game

freerails_SOURCES = i18n.cpp \
    main.cpp \
    game.cpp
