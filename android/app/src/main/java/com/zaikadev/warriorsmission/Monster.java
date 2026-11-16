package com.zaikadev.warriorsmission;

/** Monstro numa batalha (porte de Monster.h / Monster.cpp). */
public class Monster extends Personagem {
    private final Especie especie;

    public Monster(Especie especie) {
        super(especie.nome, especie.hp, 0);
        this.power = especie.power;
        this.especie = especie;
    }

    public Especie getEspecie() {
        return especie;
    }

    @Override
    public String attackFisic(Personagem inimigo) {
        int dano = calcDano();
        inimigo.receberDano(dano);
        return nome + ": atacou com sua força bruta! e causou " + dano + " de dano";
    }
}
