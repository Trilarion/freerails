#include "game.h"
#include "iostream.h"

#include "Player.h"
#include "GameElement.h"

MyGameApplication::MyGameApplication(int argc, char *argv[]):GameApplication(argc, argv) {
  // Some rather silly demonstration code:
  Player pl("me");
  cerr << "Name: " << pl.getName() << endl;
  cerr << "Adress: " << &pl << endl;
/*  GameElement element(&pl, "foo");
  element.setPlayer(&pl);
  cerr << "Adress: " << element.getPlayer() << endl;
  pl.addGameElement(&element);
*/

}

MyGameApplication::~MyGameApplication() {

}

void MyGameApplication::initGame() {

}