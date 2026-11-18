package com.zaikadev.warriorsmission;

/**
 * Tipos de monstro. Para mudar a dificuldade, mexa nos números:
 * Especie(nome, vida, força, experiência, éChefe, imagem)
 * A imagem é um PNG em res/drawable-nodpi (ex.: spr_slime.png). Para trocar o desenho,
 * basta substituir o PNG por outro com o mesmo nome (de preferência com borda preta).
 */
public final class Especie {
    public final String nome;
    public final int hp;
    public final int power;
    public final int xp;
    public final boolean chefe;
    /** Nome da imagem (em res/drawable-nodpi) usada para desenhar o monstro. */
    public final String spriteRes;

    private Especie(String nome, int hp, int power, int xp, boolean chefe, String spriteRes) {
        this.nome = nome;
        this.hp = hp;
        this.power = power;
        this.xp = xp;
        this.chefe = chefe;
        this.spriteRes = spriteRes;
    }

    public static final Especie SLIME = new Especie("Slime", 30, 8, 15, false, "spr_slime");
    public static final Especie GOBLIN = new Especie("Goblin", 80, 20, 35, false, "spr_goblin");
    public static final Especie SOLDADO = new Especie("Soldado Demônio", 160, 34, 55, false, "spr_soldado");
    public static final Especie REI = new Especie("Rei Monstro", 900, 55, 0, true, "spr_rei");
}
