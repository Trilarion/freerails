if WRAPPER_QT

noinst_LIBRARIES = libwrapper_qt.a

INCLUDES = -I$(srcdir) -I$(srcdir)/.. -I$(top_srcdir)/src/map -I$(top_srcdir)/src/elements -I$(top_srcdir)/src/controllers -I$(top_srcdir)/src/main -I$(top_srcdir)/src/network -I$(top_srcdir)/src/game $(QT_INCLUDES)

libwrapper_qt_a_SOURCES = GameApplication.cpp \
    GameDialog.cpp \
    GameMainWindow.cpp \
    GameMap.cpp \
    GameMapView.cpp \
    GameMenuBar.cpp \
    GameModeSelector.cpp \
    GamePanel.cpp \
    GameWidget.cpp \
    moc_GameMap.cpp \
    moc_GameMapView.cpp \
    moc_GameMenuBar.cpp \
    moc_GameModeSelector.cpp \
    moc_GamePanel.cpp

moc_%.cpp: %.h
	$(MOC) $< -o $@

endif
