#include "game.h"
#include "iostream.h"

#include "Player.h"
#include "GameElement.h"
#include "Train.h"

MyGameApplication::MyGameApplication(int argc, char *argv[]):GameApplication(argc, argv) {
  // Some rather silly demonstration code:
  GameController controll("default",1900,1,1);
  Player pl("me");
  cerr << "Name: " << pl.getName() << endl;
  cerr << "Adress: " << &pl << endl;
  Train train(&controll,NULL, &pl);
  train.setPlayer(&pl);
  cerr << "Adress: " << train.getPlayer() << endl;
  pl.addGameElement(&train);

}

MyGameApplication::~MyGameApplication() {

}

void MyGameApplication::initGame() {

}
