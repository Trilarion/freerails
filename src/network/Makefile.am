if WRAPPER_QT
SUBDIRS = qt
INCLUDES = -DUSE_QT -I$(srcdir)/../controllers -I$(srcdir)/../elements -I$(srcdir) $(QT_INCLUDES)
endif
if WRAPPER_PARAGUI
SUBDIRS = libSDL_net
INCLUDES = -DUSE_PARAGUI -I$(srcdir)/../controllers -I$(srcdir)/../elements -I$(srcdir) $(SDL_CFLAGS) $(PARAGUI_CFLAGS)
endif

noinst_LIBRARIES = libnetwork.a


libnetwork_a_SOURCES = Client.cpp
