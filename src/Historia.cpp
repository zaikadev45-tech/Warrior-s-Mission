#include "../header/Historia.h"
#include <unistd.h>

using namespace std;
bool skip() {
    bool exe = true;
    char escolha;
    cout << "dar skip?\n";

    while(true) {
        cout << "Y/N  R:";
        cin >> escolha;
        if(escolha == 'Y' || escolha == 'y') {
            exe = true;
            break;
        }
        else if(escolha == 'N' || escolha == 'n') {
            exe = false;
            break;
        } else {
            cout << "Invalido" << endl;
            continue;
        }
    }
    return exe;
}

void intro() {
    cout << "[ Introdução ]\n";
    if (skip() == true) {
        return;
    }

    cout << "\nEm um continente distante havia\n";
    cout << "um reino chamado ''Vergiy'' foi\n";
    sleep(1);
    cout << "aonde um garoto que futuramente\n";
    cout << "o chamariam de '' O herói ''.\n\n";
    sleep(1);
    cout << "Após o nascimento do garoto o\n";
    cout << "reino foi atacado por um exército\n";
    sleep(1);
    cout << "demônio, mais o garoto foi\n";
    cout << "levanto por um pequeno barco antes\n";
    sleep(1);
    cout << "do ataque no reino e foi levando\n";
    cout << "pra uma pequena ilha...\n\n";
    sleep(1);
    cout << "Anos após ele cresceu e ficou\n";
    cout << "forte e tinha o objetivo único de\n";
    cout << "saber aonde ele nasceu e quem\n\n";
}
