if WRAPPER_QT
WRAPPERINC = $(QT_INCLUDES)
freerails_LDFLAGS = $(QT_LDFLAGS) -lqt
endif

if WRAPPER_PARAGUI
WRAPPERINC = $(SDL_CFLAGS) $(PARAGUI_CFLAGS)
freerails_LDFLAGS = $(SDL_LIBS) $(PARAGUI_LIBS) -lSDL_ttf -lSDL_image
endif

INCLUDES = $(WRAPPERINC) -I$(srcdir) -I$(top_srcdir)/src/wrapper \
    -I$(top_srcdir)/src/wrapper/$(WRAPPER) -I$(top_srcdir) -I$(top_srcdir)/src/elements

bin_PROGRAMS = freerails

freerails_SOURCES = i18n.cpp \
    main.cpp \
    game.cpp

freerails_DEPENDENCIES = ../wrapper/$(WRAPPER)/libwrapper_$(WRAPPER).a \
    ../wrapper/libwrapperbase.a ../elements/libelements.a ../map/libmap.a
freerails_LDADD = ../wrapper/$(WRAPPER)/libwrapper_$(WRAPPER).a \
    ../wrapper/libwrapperbase.a ../elements/libelements.a ../map/libmap.a
