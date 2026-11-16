package com.zaikadev.warriorsmission;

/** Porte de Player.h / Player.cpp, agora com nível e experiência. */
public class Player extends Personagem {
    private int level = 1;
    private int xp = 0;

    public Player(String nome) {
        super(nome, 100, 40);
        power = 20;
    }

    @Override
    public String attackFisic(Personagem inimigo) {
        int dano = calcDano();
        inimigo.receberDano(dano);
        return "Heroi atacou! e causou " + dano + " de dano";
    }

    public int getLevel() { return level; }
    public int getXp() { return xp; }

    /** XP necessário para o próximo nível. */
    public int xpParaProximo() {
        return 25 * level;
    }

    /** Quanto a magia de cura recupera. */
    public int poderCura() {
        return 40 + 12 * level;
    }

    /** Ganha experiência. Devolve quantos níveis subiu. */
    public int ganharXp(int ganho) {
        xp += ganho;
        int subiu = 0;
        while (xp >= xpParaProximo()) {
            xp -= xpParaProximo();
            level++;
            maxHp += 35;
            maxMana += 12;
            power += 9;
            subiu++;
        }
        if (subiu > 0) {
            hp = maxHp;
            mana = maxMana;
        }
        return subiu;
    }

    public void restaurarTudo() {
        hp = maxHp;
        mana = maxMana;
    }
}
