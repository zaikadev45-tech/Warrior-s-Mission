package com.zaikadev.warriorsmission;

import android.graphics.Bitmap;

import java.util.Random;

/**
 * Desenhos do jogo feitos por código (pixel a pixel) enquanto os sprites finais não chegam.
 * O herói vem da imagem res/drawable-nodpi/hero_sheet.png.
 */
final class Sprites {

    private Sprites() {}

    private static final int TAM = 16;

    // ---------------------------------------------------------------- tiles

    static Bitmap[][] tiles() {
        Bitmap[][] t = new Bitmap[World.NUM_TILES][3];
        for (int id = 0; id < World.NUM_TILES; id++) {
            for (int v = 0; v < 3; v++) {
                t[id][v] = tile(id, v);
            }
        }
        return t;
    }

    private static void fill(int[] px, int cor) {
        for (int i = 0; i < px.length; i++) px[i] = cor;
    }

    private static void set(int[] px, int x, int y, int cor) {
        if (x < 0 || y < 0 || x >= TAM || y >= TAM) return;
        px[y * TAM + x] = cor;
    }

    private static void rect(int[] px, int x0, int y0, int x1, int y1, int cor) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) set(px, x, y, cor);
        }
    }

    private static void speck(int[] px, Random r, int cor, int n) {
        for (int i = 0; i < n; i++) set(px, r.nextInt(TAM), r.nextInt(TAM), cor);
    }

    private static void grama(int[] px, Random r) {
        fill(px, 0xFF4CAF3F);
        speck(px, r, 0xFF3B8F30, 10);
        speck(px, r, 0xFF6FCB55, 5);
    }

    private static void muro(int[] px, int base, int junta, int brilho) {
        fill(px, base);
        for (int y = 0; y < TAM; y++) {
            for (int x = 0; x < TAM; x++) {
                if (y % 4 == 3) {
                    set(px, x, y, junta);
                } else if (x % 8 == ((y / 4) % 2 == 0 ? 0 : 4)) {
                    set(px, x, y, junta);
                } else if (y % 4 == 0) {
                    set(px, x, y, brilho);
                }
            }
        }
    }

    private static Bitmap tile(int id, int v) {
        int[] px = new int[TAM * TAM];
        Random r = new Random(id * 1000L + v * 17L + 5L);

        switch (id) {
            case World.AGUA:
                fill(px, 0xFF2E6BD6);
                for (int k = 0; k < 5; k++) {
                    int x = r.nextInt(12);
                    int y = r.nextInt(TAM);
                    set(px, x, y, 0xFF5B93EE);
                    set(px, x + 1, y, 0xFF5B93EE);
                    set(px, x + 2, y, 0xFF5B93EE);
                }
                break;

            case World.GRAMA:
                grama(px, r);
                break;

            case World.FLOR:
                grama(px, r);
                for (int k = 0; k < 3; k++) {
                    int x = 2 + r.nextInt(12);
                    int y = 2 + r.nextInt(11);
                    int[] cores = {0xFFF06292, 0xFFFFEB3B, 0xFFFFFFFF};
                    set(px, x, y, cores[(v + k) % 3]);
                    set(px, x, y + 1, 0xFF2E7D32);
                }
                break;

            case World.ARVORE:
                grama(px, r);
                rect(px, 7, 11, 8, 15, 0xFF6D4C41);
                for (int y = 0; y < 13; y++) {
                    for (int x = 0; x < TAM; x++) {
                        double dx = x - 7.5;
                        double dy = y - 6.0;
                        double d2 = dx * dx + dy * dy * 1.1;
                        if (d2 <= 36) {
                            int cor = d2 <= 14 ? 0xFF3F9E45 : (d2 <= 26 ? 0xFF2E7D32 : 0xFF1B5E20);
                            set(px, x, y, cor);
                        }
                    }
                }
                break;

            case World.CAMINHO:
                fill(px, 0xFFC9A56A);
                speck(px, r, 0xFFB38D52, 14);
                speck(px, r, 0xFFD9BA83, 8);
                break;

            case World.AREIA:
                fill(px, 0xFFE8D9A0);
                speck(px, r, 0xFFD6C586, 12);
                speck(px, r, 0xFFF3E8B8, 6);
                break;

            case World.MONTANHA: {
                fill(px, 0xFF6B625B);
                speck(px, r, 0xFF5A524C, 12);
                int pico = 4 + v * 4;
                for (int y = 0; y < TAM; y++) {
                    for (int x = 0; x < TAM; x++) {
                        double largura = (y + 1) * 0.55;
                        if (Math.abs(x - pico) <= largura) {
                            boolean esq = x <= pico;
                            int cor;
                            if (y <= 4) {
                                cor = esq ? 0xFFF2F2F2 : 0xFFD0D6DE;
                            } else {
                                cor = esq ? 0xFF8F857C : 0xFF595049;
                            }
                            set(px, x, y, cor);
                        }
                    }
                }
                break;
            }

            case World.PISO_CASTELO:
                fill(px, 0xFFB9B6AE);
                for (int i = 0; i < TAM; i++) {
                    set(px, i, 0, 0xFF9C998F);
                    set(px, 0, i, 0xFF9C998F);
                    set(px, i, 8, 0xFF9C998F);
                    set(px, 8, i, 0xFF9C998F);
                }
                speck(px, r, 0xFFC8C5BD, 6);
                break;

            case World.MURO_CASTELO:
                muro(px, 0xFF6E6A78, 0xFF4B4856, 0xFF8A8697);
                break;

            case World.PAREDE_CASA:
                fill(px, 0xFFD9C28F);
                for (int y = 0; y < TAM; y++) set(px, 3, y, 0xFFB9A06B);
                for (int y = 0; y < TAM; y++) set(px, 7, y, 0xFFB9A06B);
                for (int y = 0; y < TAM; y++) set(px, 11, y, 0xFFB9A06B);
                for (int y = 0; y < TAM; y++) set(px, 15, y, 0xFFB9A06B);
                rect(px, 0, 14, 15, 15, 0xFF9C8450);
                if (v == 1) {
                    rect(px, 4, 3, 11, 10, 0xFF6D4C41);
                    rect(px, 5, 4, 10, 9, 0xFF8EC5F0);
                    rect(px, 7, 4, 8, 9, 0xFF6D4C41);
                }
                break;

            case World.PORTA:
                fill(px, 0xFFD9C28F);
                rect(px, 0, 14, 15, 15, 0xFF9C8450);
                rect(px, 3, 1, 12, 15, 0xFF4A2A10);
                rect(px, 4, 2, 11, 15, 0xFF7A4A22);
                rect(px, 8, 2, 8, 15, 0xFF5E3718);
                set(px, 10, 9, 0xFFFFD21E);
                break;

            case World.TELHADO:
                fill(px, 0xFFB5412D);
                for (int y = 0; y < TAM; y++) {
                    for (int x = 0; x < TAM; x++) {
                        if (y % 4 == 3) {
                            set(px, x, y, 0xFF8E3020);
                        } else if (x % 8 == ((y / 4) % 2 == 0 ? 2 : 6)) {
                            set(px, x, y, 0xFF8E3020);
                        } else if (y % 4 == 0) {
                            set(px, x, y, 0xFFC9573F);
                        }
                    }
                }
                break;

            case World.TERRA_ESCURA:
                fill(px, 0xFF3B2F3F);
                speck(px, r, 0xFF2A2030, 14);
                speck(px, r, 0xFF54405A, 8);
                speck(px, r, 0xFF6B2A2A, 2);
                break;

            case World.LAVA:
                fill(px, 0xFFD8481A);
                speck(px, r, 0xFFFF9A2E, 10);
                speck(px, r, 0xFF8F2410, 8);
                speck(px, r, 0xFFFFD21E, 2);
                break;

            case World.TERRA_GUERRA:
                fill(px, 0xFF8B7355);
                speck(px, r, 0xFF6E5A42, 14);
                speck(px, r, 0xFFA38A68, 8);
                if (v == 1) {
                    set(px, 2, 3, 0xFF4D3E2E);
                    set(px, 3, 4, 0xFF4D3E2E);
                    set(px, 4, 4, 0xFF4D3E2E);
                    set(px, 5, 5, 0xFF4D3E2E);
                    set(px, 6, 6, 0xFF4D3E2E);
                }
                break;

            case World.MURO_DEMONIO:
                muro(px, 0xFF2B1E33, 0xFF180F20, 0xFF3C2A47);
                speck(px, r, 0xFF8C1C1C, 3);
                break;

            case World.PISO_ESCURO:
                fill(px, 0xFF2A2234);
                for (int i = 0; i < TAM; i++) {
                    set(px, i, 0, 0xFF3E3250);
                    set(px, 0, i, 0xFF3E3250);
                    set(px, i, 8, 0xFF3E3250);
                    set(px, 8, i, 0xFF3E3250);
                }
                speck(px, r, 0xFF1C1624, 6);
                break;

            case World.PEDRA:
                grama(px, r);
                for (int y = 5; y < 15; y++) {
                    for (int x = 2; x < 14; x++) {
                        double dx = (x - 7.5) / 6.0;
                        double dy = (y - 10.0) / 4.5;
                        if (dx * dx + dy * dy <= 1.0) {
                            int cor = dy < -0.3 ? 0xFFB5B5B5 : (dy > 0.4 ? 0xFF5E5E5E : 0xFF8A8A8A);
                            set(px, x, y, cor);
                        }
                    }
                }
                break;

            default:
                fill(px, 0xFFFF00FF);
        }

        Bitmap b = Bitmap.createBitmap(TAM, TAM, Bitmap.Config.ARGB_8888);
        b.setPixels(px, 0, TAM, 0, 0, TAM, TAM);
        return b;
    }

    // ---------------------------------------------------------------- monstros

    static Bitmap deEspecie(Especie e) {
        int[] px = new int[TAM * TAM];
        for (int y = 0; y < TAM; y++) {
            String linha = e.sprite[y];
            for (int x = 0; x < TAM; x++) {
                char ch = linha.charAt(x);
                if (ch != '.') px[y * TAM + x] = e.cor(ch);
            }
        }
        Bitmap b = Bitmap.createBitmap(TAM, TAM, Bitmap.Config.ARGB_8888);
        b.setPixels(px, 0, TAM, 0, 0, TAM, TAM);
        return b;
    }

    // ---------------------------------------------------------------- pessoas

    private static final String[] NPC = {
            "................",
            "....hhhhhhhh....",
            "....hhhhhhhh....",
            "....hssssssh....",
            "....hskssksh....",
            "....hssssssh....",
            ".....ssssss.....",
            "....cccccccc....",
            "...cccccccccc...",
            "..sccccccccccs..",
            "..sccccccccccs..",
            "...cccccccccc...",
            "....dd....dd....",
            "....dd....dd....",
            "....bb....bb....",
            "................"
    };

    static Bitmap npc(int roupa, int cabelo) {
        int[] px = new int[TAM * TAM];
        for (int y = 0; y < TAM; y++) {
            for (int x = 0; x < TAM; x++) {
                int cor;
                switch (NPC[y].charAt(x)) {
                    case 'h': cor = cabelo; break;
                    case 's': cor = 0xFFF8C8A0; break;
                    case 'k': cor = 0xFF101010; break;
                    case 'c': cor = roupa; break;
                    case 'd': cor = 0xFF3A3A4A; break;
                    case 'b': cor = 0xFF4A2A10; break;
                    default:  cor = 0;
                }
                px[y * TAM + x] = cor;
            }
        }
        Bitmap b = Bitmap.createBitmap(TAM, TAM, Bitmap.Config.ARGB_8888);
        b.setPixels(px, 0, TAM, 0, 0, TAM, TAM);
        return b;
    }
}
