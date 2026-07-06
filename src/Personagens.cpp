#include "../header/Personagens.h"
#include <cstdlib>
#include <ctime>

using namespace std;
Personagem::Personagem(string nome, int HP, int MANA) {
    this->nome = nome;

    this->HP = HP;
    this->MAX_HP = HP;

    this->MANA = MANA;
    this->MAX_MANA = MANA;
}

void Personagem::cura(int custo, int use_mana) {
    if(MANA < custo) {
        cout << "você não tem mana suficiente" << endl;
        return;
    }
    MANA -= custo;
    HP += use_mana;
    if(HP > MAX_HP) HP = MAX_HP;
    cout << "Recuperou " << use_mana << " de HP" << endl;
}

int Personagem::calc_dano() {
    int calc = rand() % 11 - 5;
    return power + calc;
}
void Personagem::ReceberDano(int dano) {
    HP -= dano;
    if(HP <= 0) HP = 0;
}





void Personagem::status() {
    cout << nome << ": " << HP << '/' << MAX_HP << endl;
    cout <<  "mana: " << MANA << '/' << MAX_MANA << endl;
}

bool Personagem::life() {
    return HP > 0;
}
