package com.zaikadev.warriorsmission;

/** Porte de Monster.h / Monster.cpp (C++) */
public class Monster extends Personagem {
    public Monster(String nome, int hp, int power) {
        super(nome, hp, 0);
        this.power = power;
    }

    @Override
    public String attackFisic(Personagem inimigo) {
        int dano = calcDano();
        inimigo.receberDano(dano);
        return nome + ": atacou com sua força bruta! e causou " + dano + " de dano";
    }
}
