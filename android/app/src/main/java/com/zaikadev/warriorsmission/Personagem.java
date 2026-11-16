package com.zaikadev.warriorsmission;

import java.util.Random;

/** Porte de Personagens.h / Personagens.cpp (C++) */
public abstract class Personagem {
    protected static final Random RND = new Random();

    protected String nome;
    protected int power;

    protected int hp, maxHp;
    protected int mana, maxMana;

    public Personagem(String nome, int hp, int mana) {
        this.nome = nome;
        this.hp = hp;
        this.maxHp = hp;
        this.mana = mana;
        this.maxMana = mana;
    }

    /** Ataca o inimigo e devolve a mensagem para mostrar na tela. */
    public abstract String attackFisic(Personagem inimigo);

    public String cura(int custo, int quantidade) {
        if (mana < custo) {
            return "você não tem mana suficiente";
        }
        mana -= custo;
        int antes = hp;
        hp += quantidade;
        if (hp > maxHp) hp = maxHp;
        return "Recuperou " + (hp - antes) + " de HP";
    }

    public int calcDano() {
        int calc = RND.nextInt(11) - 5;
        return power + calc;
    }

    public void receberDano(int dano) {
        hp -= dano;
        if (hp <= 0) hp = 0;
    }

    public boolean life() {
        return hp > 0;
    }

    public String getNome() { return nome; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getMana() { return mana; }
    public int getMaxMana() { return maxMana; }
}
