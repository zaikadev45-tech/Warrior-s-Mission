package com.zaikadev.warriorsmission;

/** Monstro visível andando pelo mapa. */
public class Inimigo {
    public final int id;
    public final Especie especie;
    public int x, y;            // tile onde está (destino do passo atual)
    public int fromX, fromY;    // tile de onde saiu
    public long moveStart;
    public boolean moving;
    public final int homeX, homeY;
    public long nextAction;
    public boolean fixo;        // chefes não andam

    public Inimigo(int id, Especie especie, int x, int y) {
        this.id = id;
        this.especie = especie;
        this.x = x;
        this.y = y;
        this.fromX = x;
        this.fromY = y;
        this.homeX = x;
        this.homeY = y;
    }

    private float lerp(int from, int to, long now) {
        if (!moving) return to;
        float t = (now - moveStart) / (float) World.PASSO_MONSTRO;
        if (t < 0f) t = 0f;
        if (t > 1f) t = 1f;
        return from + (to - from) * t;
    }

    /** Posição em pixels (canto esquerdo) já suavizada. */
    public float px(long now) { return lerp(fromX, x, now) * 16f; }
    public float py(long now) { return lerp(fromY, y, now) * 16f; }
}
