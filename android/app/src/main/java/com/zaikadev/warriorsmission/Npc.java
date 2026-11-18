package com.zaikadev.warriorsmission;

/** Pessoa do mapa. Quando o herói encosta, ela fala (cada fala é uma caixa de texto). */
public class Npc {
    public final int x, y;
    public final String nome;
    public final String sprite;  // imagem: res/drawable-nodpi/npc_<sprite>.png
    public final boolean cura;   // se true, restaura HP e MP do herói
    public final String[] falas;

    public Npc(int x, int y, String nome, String sprite, boolean cura, String... falas) {
        this.x = x;
        this.y = y;
        this.nome = nome;
        this.sprite = sprite;
        this.cura = cura;
        this.falas = falas;
    }
}
