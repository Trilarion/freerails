if WRAPPER_PARAGUI

noinst_LIBRARIES = libwrapper_paragui.a

INCLUDES = -I$(srcdir) -I$(srcdir)/.. -I$(top_srcdir)/src/main $(SDL_CFLAGS) $(PARAGUI_CFLAGS)

libwrapper_paragui_a_SOURCES = GameApplication.cpp \
    GameDialog.cpp \
    GameMainWindow.cpp \
    GameMapView.cpp \
    GameModeSelectDialog.cpp \
    GamePanel.cpp \
    GameWidget.cpp

endif
