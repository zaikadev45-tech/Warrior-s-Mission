#ifndef PERSONAGENS_H
#define PERSONAGENS_H

// zaika DEV 
#include <iostream>
#include <string>
using namespace std;

class Personagem {
protected:
    //base
    string nome;
    int power;

    int HP, MAX_HP;
    int MANA, MAX_MANA;
public:
    Personagem(string nome, int HP, int mana);

    //combate
    
    virtual void attack_fisic(Personagem* inimigo) = 0;
    void cura(int custo, int MAX_MANA);

    int calc_dano();
    void ReceberDano(int dano);

    void status();

    bool life();
    bool cust_mana(int custo);

    virtual ~Personagem() {}
};


#endif
