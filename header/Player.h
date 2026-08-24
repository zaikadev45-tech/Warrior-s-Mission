#ifndef PLAYER_H
#define PLAYER_H

#include "Personagens.h"

class Player : public Personagem {
public:
    Player(string nome);
    void escolha(Personagem* inimigo);
    void attack_fisic(Personagem* inimigo) override;
};

#endif
