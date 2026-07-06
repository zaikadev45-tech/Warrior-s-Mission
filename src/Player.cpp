#include "../header/Player.h"
#include "../header/Personagens.h"
Player::Player(string nome)
    : Personagem(nome, 500, 400){
        power = 200;
}
void Player::attack_fisic(Personagem* inimigo) {
    int dano = calc_dano();
    inimigo->ReceberDano(dano);
    cout << "Heroi atacou! e causou " << dano << " de dano" << endl;
}
