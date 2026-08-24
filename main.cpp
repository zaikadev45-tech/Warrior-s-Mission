#include <iostream>
#include <thread>
#include <chrono>

#include "header/Historia.h"
#include "header/Player.h"
#include "header/Monster.h"

using namespace std;

void batalha(Player* player, Monster* monster);

int main()
{
    srand(time(0));
    cout << "=====[ RPG - warrior's mission ]=====" << endl;
    intro();

    Player heroi("Heroi");
    Monster* monstro = new Monster("Monster tester", 1000, 60);
    batalha(&heroi, monstro);
    //delete monstro;

    cout << "=====[   FIM   ]=====" << endl;
    return 0;
}

void batalha(Player* player, Monster *monster) {
    cout << "=====[ BATALHA ]=====" << endl;
    while(true) {
        monster->attack_fisic(player);
        player->status();

        if(!player->life()) {
            cout << "Heroi morreu..." << endl;
            break;
        }

        player->escolha(monster);

        if(!monster->life()) {
            cout << "Você venceu!" << endl;
            delete monster;
            break;
        }

        cout << "próxino turno..." << endl;
        this_thread::sleep_for(chrono::seconds(2));
        system("clear");
    }
}
