#ifndef MONSTER_H
#define MONSTER_H

#include "Personagens.h"

class Monster : public Personagem {
public:
    Monster(string nome, int HP, int power);
    void attack_fisic(Personagem* inimigo) override;
};

#endif
