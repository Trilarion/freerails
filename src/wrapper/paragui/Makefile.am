noinst_LIBRARIES = libwrapper.a

INCLUDES = -I$(srcdir) -I$(srcdir)/.. -I$(top_srcdir)/src/main \
	-I$(top_srcdir)/src/map -I$(top_srcdir)/src/elements \
	-I$(top_srcdir)/src/common -I$(top_srcdir)/src/controllers \
	-I$(top_srcdir)/src/network -I$(top_srcdir)/src/game \
	$(SDL_CFLAGS) $(PARAGUI_CFLAGS)

libwrapper_a_SOURCES = GameApplication.cpp \
    GameDataSelectDialog.cpp \
    GameDialog.cpp \
    GameMainWindow.cpp \
    GameMapView.cpp \
    GameNetView.cpp \
    GameModeSelectDialog.cpp \
    GamePanel.cpp \
    GameWidget.cpp \
    GameNetHandler.cpp \
    GameFrameHandler.cpp \
    \
    TerrainInfoPane.cpp \
    TerrainBuildPane.cpp \
    MapHelper.cpp \
    TrainFrame.cpp \
    \
    pgframeapplication.cpp \
    pgframeobject.cpp \
    pgspriteobject.cpp \
    pgspritebase.cpp \
    pgframehandler.cpp \
    pgnethandler.cpp

#    TrainFrame.cpp \
#    pgframeapplicationhandler.cpp 
