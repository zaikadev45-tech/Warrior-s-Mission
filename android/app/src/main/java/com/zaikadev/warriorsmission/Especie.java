package com.zaikadev.warriorsmission;

/**
 * Tipos de monstro. Para mudar a dificuldade, mexa nos números:
 * Especie(nome, vida, força, experiência, éChefe, desenho, letras, cores)
 * O desenho tem 16x16 "pixels"; cada letra é uma cor (veja a lista de letras/cores).
 */
public final class Especie {
    public final String nome;
    public final int hp;
    public final int power;
    public final int xp;
    public final boolean chefe;
    public final String[] sprite;
    private final String chaves;
    private final int[] cores;

    private Especie(String nome, int hp, int power, int xp, boolean chefe,
                    String[] sprite, String chaves, int[] cores) {
        this.nome = nome;
        this.hp = hp;
        this.power = power;
        this.xp = xp;
        this.chefe = chefe;
        this.sprite = sprite;
        this.chaves = chaves;
        this.cores = cores;
    }

    /** Cor (ARGB) da letra usada no desenho. */
    public int cor(char ch) {
        int i = chaves.indexOf(ch);
        return i < 0 ? 0xFFFF00FF : cores[i];
    }

    public static final Especie SLIME = new Especie("Slime", 30, 8, 15, false,
            new String[]{
                    "................",
                    "................",
                    "................",
                    "................",
                    ".....dddddd.....",
                    "...ddgggggggdd..",
                    "..dgglllggggggd.",
                    ".dggllggggggggd.",
                    ".dgwwkggggwwkgd.",
                    ".dgwwkggggwwkgd.",
                    ".dggggggggggggd.",
                    ".dgggkkkkkkgggd.",
                    "..dggggggggggd..",
                    "...dddddddddd...",
                    "................",
                    "................"
            },
            "gdlwk",
            new int[]{0xFF4CC24A, 0xFF2A7A2E, 0xFF9BE58F, 0xFFFFFFFF, 0xFF102010});

    public static final Especie GOBLIN = new Especie("Goblin", 80, 20, 35, false,
            new String[]{
                    "................",
                    "................",
                    "..dg.........gd.",
                    "..ggg......ggg..",
                    "...dggggggggdg..",
                    "...dgykggkygd...",
                    "....dgggggggg...",
                    ".....gwkwkwg....",
                    "....nnnnnnnnnn..",
                    "...nnnNnnnnNnnn.",
                    "..ggnnnnnnnnngg.",
                    "..ggnnnNNnnnngg.",
                    "....nnn..nnn....",
                    "....NNN..NNN....",
                    "...NNNN..NNNN...",
                    "................"
            },
            "gdykwnN",
            new int[]{0xFF7CB342, 0xFF4E7A22, 0xFFFFD21E, 0xFF140A0A, 0xFFF2F2F2, 0xFF8D5A2B, 0xFF5A3A1A});

    public static final Especie SOLDADO = new Especie("Soldado Demônio", 160, 34, 55, false,
            new String[]{
                    "..w..........w..",
                    "..ww........ww..",
                    "...rd......dr...",
                    "...rrrrrrrrrr...",
                    "..rrrrrrrrrrrr..",
                    "..rryyrrrryyrr..",
                    "..rrrrrddrrrrr..",
                    "...rrkkkkkkrr...",
                    "..EEeeEEeeEEee..",
                    ".rEeeEEeeEEeeEr.",
                    ".rrEEeeEEeeEEerr",
                    "..rEEeeEEeeEEr..",
                    "....EEE....EEE..",
                    "....EEE....EEE..",
                    "...kkkk....kkkk.",
                    "................"
            },
            "rdkyeEw",
            new int[]{0xFFB02A1E, 0xFF6E1010, 0xFF140A0A, 0xFFFFD21E, 0xFF8C93A0, 0xFF4E5461, 0xFFF2F2F2});

    public static final Especie REI = new Especie("Rei Monstro", 900, 55, 0, true,
            new String[]{
                    "..w..........w..",
                    "..ww........ww..",
                    "..wrd......drw..",
                    "...rrdddddddrr..",
                    "..rrrrrrrrrrrrr.",
                    ".rrrrrrrrrrrrrrr",
                    ".rryyyrrrrryyyrr",
                    ".rrkykrrrrrkykrr",
                    ".rrrrrrrddrrrrrr",
                    ".rrrrrrrrrrrrrrr",
                    ".rkwwkwwkwwkwwkr",
                    "..rkkkkkkkkkkkr.",
                    "..rrrrrrrrrrrrr.",
                    "...rrrddddddrr..",
                    "....ddddddddd...",
                    "................"
            },
            "rdwyk",
            new int[]{0xFFC8321E, 0xFF7A1010, 0xFFF2F2F2, 0xFFFFD21E, 0xFF140A0A});
}
