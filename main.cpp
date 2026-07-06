#include <iostream>

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
    Monster* monstro = new Monster("Rei Monstro", 1000, 60);

    batalha(&heroi, monstro);
    delete monstro;

    cout << "=====[   FIM   ]=====" << endl;
    return 0;
}

void batalha(Player* player, Monster *monster) {
    cout << "=====[ BATALHA ]=====" << endl;
    char escolha = '1';
    while(true) {
        monster->attack_fisic(player);
        player->status();

        if(!player->life()) {
            cout << "Heroi morreu..." << endl;
            break;
        }

        cout << "\n[1] Atacar  [2] Curar = R: ";
        cin >> escolha;
        cout << "\n\n";

        cout << "[+]=======================[+]" << endl;
        switch (escolha) {
            case '1':
                player->attack_fisic(monster);
                monster->status();
                cout << endl;
                break;
            case '2':
                player->cura(100, 200);
                break;
            default:
                continue;
        }
        if(!monster->life()) {
            cout << "Você venceu!" << endl;
            break;
        }
    }
}
