package com.zaikadev.warriorsmission;

/** Porte de Player.h / Player.cpp (C++) */
public class Player extends Personagem {
    public Player(String nome) {
        super(nome, 500, 400);
        power = 200;
    }

    @Override
    public String attackFisic(Personagem inimigo) {
        int dano = calcDano();
        inimigo.receberDano(dano);
        return "Heroi atacou! e causou " + dano + " de dano";
    }
}
