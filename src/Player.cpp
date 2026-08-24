#include "../header/Player.h"
#include "../header/Personagens.h"
#include <ios>
#include <iostream>
#include <limits>
Player::Player(string nome)
    : Personagem(nome, 500, 400){
        power = 200;
}
void Player::attack_fisic(Personagem* inimigo) {
    int dano = calc_dano();
    inimigo->ReceberDano(dano);
    cout << "Heroi atacou! e causou " << dano << " de dano" << endl;
}

void Player::escolha(Personagem* inimigo) {
    int escolha = 0;

    do {
        cout << "\n[1] Atacar  [2] Curar = R: ";
        if(!(cin >> escolha))
        {
            cin.clear();
            cin.ignore(numeric_limits<streamsize>::max(), '\n');
            cout << "Digite apenas numeros!" << endl;
            continue;
        }

        if(escolha >= 1 && escolha <= 2) break;
    } while(true);

    
    switch (escolha)
    {
        case 1:
            attack_fisic(inimigo);
            inimigo->status();
            cout << endl;
            break;
        case 2:
            cura(100, 200);
            break;
    }
    cout << "[+]=======================[+]" << endl;

    //espaço para debug
    //cout << "escolha: " << escolha;
}
