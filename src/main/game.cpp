#include "game.h"

#ifdef USE_PARAGUI
Game_Application::Game_Application(int argc, char *argv[]): WrapperParaGUI(argc,argv) {
#endif

#ifdef USE_QT
Game_Application::Game_Application(int argc, char *argv[]): WrapperQt(argc,argv) {
#endif

}

Game_Application::~Game_Application() {

}

void Game_Application::InitGame() {

}

void Game_Application::AskUser() {

}
