if WRAPPER_PARAGUI

noinst_LIBRARIES = libwrapper_paragui.a

INCLUDES = -I$(srcdir) -I$(srcdir)/.. -I$(top_srcdir)/src/main -I$(top_srcdir)/src/map -I$(top_srcdir)/src/elements -I$(top_srcdir)/src/controllers $(SDL_CFLAGS) $(PARAGUI_CFLAGS)

libwrapper_paragui_a_SOURCES = GameApplication.cpp \
    GameDialog.cpp \
    GameMainWindow.cpp \
    GameMapView.cpp \
    GameNetView.cpp \
    GameModeSelectDialog.cpp \
    GamePanel.cpp \
    GameWidget.cpp

endif
