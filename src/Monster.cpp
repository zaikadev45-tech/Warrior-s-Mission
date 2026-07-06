#include "../header/Monster.h"
#include "../header/Personagens.h"

Monster::Monster(string nome, int HP, int power)
    : Personagem(nome, HP, 0) {
        this->power = power;
}

void Monster::attack_fisic(Personagem* inimigo) {
    int dano = calc_dano();
    inimigo->ReceberDano(dano);
    cout << nome <<  ": atacou com sua força bruta! e causou " << dano << " de dano" << endl;
}
