noinst_LIBRARIES = libwrapper.a

INCLUDES = -I$(srcdir) -I$(srcdir)/.. -I$(top_srcdir)/src/map \
    -I$(top_srcdir)/src/elements -I$(top_srcdir)/src/controllers \
    -I$(top_srcdir)/src/main -I$(top_srcdir)/src/network \
    -I$(top_srcdir)/src/game $(QT_INCLUDES)

libwrapper_a_METASOURCES = AUTO

libwrapper_a_SOURCES = \
    GameApplication.cpp \
    GameDialog.cpp \
    GameMainWindow.cpp \
    GameMap.cpp \
    GameMapView.cpp \
    GameMenuBar.cpp \
    GameModeSelector.cpp \
    GamePanel.cpp \
    GameWidget.cpp singlegameoptiondialog.cpp
