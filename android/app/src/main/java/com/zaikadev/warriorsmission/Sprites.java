package com.zaikadev.warriorsmission;

import android.graphics.Bitmap;

import java.util.Random;

/**
 * Desenhos do jogo feitos por código (pixel a pixel) enquanto os sprites finais não chegam.
 * Tiles e personagens do mundo têm 32x32 pixels. O herói vem da imagem hero_sheet.png.
 */
final class Sprites {

    private Sprites() {}

    static final int T = 32;

    // ---------------------------------------------------------------- utilitários

    private static void fill(int[] p, int cor) {
        for (int i = 0; i < p.length; i++) p[i] = cor;
    }

    /** Pinta um pixel; fora do tile "dá a volta" (assim o desenho emenda com o vizinho). */
    private static void set(int[] p, int x, int y, int cor) {
        x = ((x % T) + T) % T;
        y = ((y % T) + T) % T;
        p[y * T + x] = cor;
    }

    private static void rect(int[] p, int x0, int y0, int x1, int y1, int cor) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) set(p, x, y, cor);
        }
    }

    private static void circ(int[] p, int cx, int cy, int r, int cor) {
        for (int y = cy - r; y <= cy + r; y++) {
            for (int x = cx - r; x <= cx + r; x++) {
                if ((x - cx) * (x - cx) + (y - cy) * (y - cy) <= r * r + r / 2) set(p, x, y, cor);
            }
        }
    }

    private static void ell(int[] p, int cx, int cy, int rx, int ry, int cor) {
        for (int y = cy - ry; y <= cy + ry; y++) {
            for (int x = cx - rx; x <= cx + rx; x++) {
                double dx = (x - cx) / (double) rx;
                double dy = (y - cy) / (double) ry;
                if (dx * dx + dy * dy <= 1.0) set(p, x, y, cor);
            }
        }
    }

    private static void line(int[] p, int x0, int y0, int x1, int y1, int cor) {
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        while (true) {
            set(p, x0, y0, cor);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 < dx) { err += dx; y0 += sy; }
        }
    }

    private static void speck(int[] p, Random r, int cor, int n) {
        for (int i = 0; i < n; i++) set(p, r.nextInt(T), r.nextInt(T), cor);
    }

    private static int shade(int cor, float f) {
        int r = Math.min(255, Math.max(0, (int) (((cor >> 16) & 255) * f)));
        int g = Math.min(255, Math.max(0, (int) (((cor >> 8) & 255) * f)));
        int b = Math.min(255, Math.max(0, (int) ((cor & 255) * f)));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static Bitmap bitmap(int[] p, int w, int h) {
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        b.setPixels(p, 0, w, 0, 0, w, h);
        return b;
    }

    // ---------------------------------------------------------------- tiles

    static Bitmap[][] tiles() {
        Bitmap[][] t = new Bitmap[World.NUM_TILES][3];
        for (int id = 0; id < World.NUM_TILES; id++) {
            for (int v = 0; v < 3; v++) {
                t[id][v] = bitmap(tilePx(id, v), T, T);
            }
        }
        return t;
    }

    private static void grama(int[] p, Random r) {
        fill(p, 0xFF5DC24B);
        for (int k = 0; k < 6; k++) circ(p, r.nextInt(T), r.nextInt(T), 3 + r.nextInt(3), 0xFF52B342);
        for (int k = 0; k < 4; k++) circ(p, r.nextInt(T), r.nextInt(T), 2 + r.nextInt(2), 0xFF6ED458);
        for (int k = 0; k < 8; k++) {
            int x = 2 + r.nextInt(28), y = 3 + r.nextInt(27);
            set(p, x, y, 0xFF3F9A33);
            set(p, x - 1, y - 1, 0xFF3F9A33);
            set(p, x + 1, y - 1, 0xFF3F9A33);
        }
        for (int k = 0; k < 6; k++) {
            int x = 2 + r.nextInt(28), y = 3 + r.nextInt(27);
            set(p, x, y, 0xFF8DE56C);
            set(p, x, y - 1, 0xFF8DE56C);
        }
    }

    private static void terraEscura(int[] p, Random r) {
        fill(p, 0xFF3A2C45);
        for (int k = 0; k < 6; k++) circ(p, r.nextInt(T), r.nextInt(T), 3 + r.nextInt(3), 0xFF2C2036);
        for (int k = 0; k < 4; k++) circ(p, r.nextInt(T), r.nextInt(T), 2, 0xFF4A3A58);
        for (int k = 0; k < 3; k++) {
            int x = r.nextInt(T), y = r.nextInt(T);
            line(p, x, y, x + 3 + r.nextInt(4), y + 2 + r.nextInt(4), 0xFF1B1224);
        }
        for (int k = 0; k < 3; k++) {
            int x = r.nextInt(T), y = r.nextInt(T);
            set(p, x, y, 0xFFFF5A2A);
            set(p, x + 1, y, 0xFF8C2A2A);
            set(p, x, y + 1, 0xFF8C2A2A);
        }
    }

    private static void guerraBase(int[] p, Random r) {
        fill(p, 0xFF9A7E58);
        for (int k = 0; k < 6; k++) circ(p, r.nextInt(T), r.nextInt(T), 3 + r.nextInt(3), 0xFF86694A);
        for (int k = 0; k < 4; k++) circ(p, r.nextInt(T), r.nextInt(T), 2 + r.nextInt(2), 0xFFAD9468);
        for (int k = 0; k < 3; k++) {
            int x = r.nextInt(T), y = r.nextInt(T);
            line(p, x, y, x + 3 + r.nextInt(5), y + 3 + r.nextInt(4), 0xFF5A4630);
        }
        speck(p, r, 0xFF6E5A42, 10);
    }

    private static void telhado(int[] p, int base) {
        fill(p, base);
        for (int row = 0; row < 4; row++) {
            int y0 = row * 8;
            int off = (row % 2) * 8;
            for (int s = -1; s < 3; s++) {
                int x0 = s * 16 + off;
                int cor = ((s + row) % 2 == 0) ? base : shade(base, 0.9f);
                rect(p, x0, y0, x0 + 15, y0 + 7, cor);
                rect(p, x0, y0, x0 + 15, y0, shade(base, 1.25f));        // luz no topo
                rect(p, x0, y0 + 6, x0 + 15, y0 + 7, shade(base, 0.62f)); // sombra embaixo
                rect(p, x0, y0, x0, y0 + 7, shade(base, 0.7f));          // emenda
            }
        }
    }

    private static void muroBase(int[] p, int base, int junta, int luz) {
        fill(p, base);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                int linha = y / 8;
                int off = (linha % 2) * 8;
                if (y % 8 == 7) {
                    set(p, x, y, junta);
                } else if ((x + off) % 16 == 15) {
                    set(p, x, y, junta);
                } else if (y % 8 == 0) {
                    set(p, x, y, luz);
                }
            }
        }
    }

    private static int[] tilePx(int id, int v) {
        int[] p = new int[T * T];
        boolean animado = (id == World.AGUA || id == World.LAVA);
        Random r = new Random(id * 1000L + (animado ? 0 : v * 17L) + 5L);

        switch (id) {
            case World.AGUA:
                fill(p, 0xFF2E8BEA);
                for (int k = 0; k < 5; k++) circ(p, r.nextInt(T), r.nextInt(T), 4 + r.nextInt(3), 0xFF2679DA);
                for (int k = 0; k < 4; k++) circ(p, r.nextInt(T), r.nextInt(T), 2 + r.nextInt(2), 0xFF4FA6F5);
                for (int k = 0; k < 6; k++) {
                    int y = (k * 6 + 3 + v * 2) % T;
                    int x = (k * 11 + v * 5 + 4) % T;
                    for (int i = 0; i < 6; i++) set(p, x + i, y + ((i == 0 || i == 5) ? 1 : 0), 0xFF9AD8FF);
                }
                for (int k = 0; k < 4; k++) set(p, (k * 9 + v * 7 + 2) % T, (k * 13 + v * 3 + 5) % T, 0xFFFFFFFF);
                break;

            case World.GRAMA:
                grama(p, r);
                break;

            case World.FLOR: {
                grama(p, r);
                int[] cores = {0xFFFF6FA5, 0xFFFFE04A, 0xFFFFFFFF, 0xFFB77CFF};
                for (int k = 0; k < 5; k++) {
                    int x = 3 + r.nextInt(26), y = 3 + r.nextInt(26);
                    int c = cores[(v + k) % 4];
                    set(p, x - 1, y, c); set(p, x + 1, y, c);
                    set(p, x, y - 1, c); set(p, x, y + 1, c);
                    set(p, x, y, k % 2 == 0 ? 0xFFFFD21E : 0xFFFFFFFF);
                }
                break;
            }

            case World.GRAMA_ALTA:
                grama(p, r);
                for (int k = 0; k < 16; k++) {
                    int x = 2 + r.nextInt(28), y = 10 + r.nextInt(20);
                    int h = 5 + r.nextInt(5);
                    int lean = r.nextInt(3) - 1;
                    line(p, x, y, x + lean * 2, y - h, k % 3 == 0 ? 0xFF7EDB5E : 0xFF2F8A2A);
                }
                break;

            case World.ARVORE: {
                grama(p, r);
                ell(p, 16, 28, 12, 4, 0xFF3A8C2F);                       // sombra
                rect(p, 14, 18, 17, 28, 0xFF7B4F2C);                      // tronco
                rect(p, 14, 18, 14, 28, 0xFF9A6A3C);
                rect(p, 17, 18, 17, 28, 0xFF5A3A1E);
                rect(p, 12, 27, 14, 28, 0xFF5A3A1E);
                rect(p, 17, 27, 20, 28, 0xFF5A3A1E);
                int escuro = v == 1 ? 0xFF3A9A2E : 0xFF2A8238;
                int medio = v == 1 ? 0xFF58BE3A : 0xFF3FAF4A;
                int claro = v == 1 ? 0xFF8BE060 : 0xFF63CD62;
                circ(p, 16, 11, 11, shade(escuro, 0.85f));                // copa
                circ(p, 9, 14, 7, escuro);
                circ(p, 23, 14, 7, escuro);
                circ(p, 16, 10, 9, medio);
                circ(p, 11, 12, 6, medio);
                circ(p, 21, 12, 6, medio);
                circ(p, 13, 8, 6, claro);
                circ(p, 10, 6, 3, 0xFF9DEF8A);
                for (int k = 0; k < 6; k++) set(p, 6 + r.nextInt(20), 4 + r.nextInt(14), 0xFF2A8238);
                if (v == 2) {                                              // macieira
                    int[][] m = {{9, 13}, {16, 7}, {22, 12}, {14, 17}, {20, 18}};
                    for (int[] q : m) {
                        circ(p, q[0], q[1], 1, 0xFFE0382E);
                        set(p, q[0] - 1, q[1] - 1, 0xFFFF9A8A);
                    }
                }
                break;
            }

            case World.PINHEIRO: {
                grama(p, r);
                ell(p, 16, 29, 9, 3, 0xFF3A8C2F);
                rect(p, 15, 24, 17, 30, 0xFF6A4528);
                for (int camada = 0; camada < 3; camada++) {
                    int topo = 2 + camada * 7;
                    int meia = 6 + camada * 3;
                    for (int y = 0; y < 11; y++) {
                        int larg = (int) Math.round(meia * (y + 1) / 11.0);
                        for (int x = -larg; x <= larg; x++) {
                            int cor = x < 0 ? 0xFF2E9A4A : 0xFF1B6A33;
                            if (Math.abs(x) >= larg - 1) cor = x < 0 ? 0xFF53C46A : 0xFF124F26;
                            set(p, 16 + x, topo + y, cor);
                        }
                    }
                    if (v == 2) {
                        for (int x = -2; x <= 2; x++) set(p, 16 + x, topo + 3, 0xFFFFFFFF);
                        set(p, 16, topo + 1, 0xFFFFFFFF);
                    }
                }
                break;
            }

            case World.ARVORE_MORTA: {
                terraEscura(p, r);
                ell(p, 16, 29, 10, 3, 0xFF1B1224);
                rect(p, 14, 12, 18, 29, 0xFF2A1A20);
                rect(p, 14, 12, 14, 29, 0xFF4A2F3A);
                line(p, 15, 18, 6, 8, 0xFF2A1A20);
                line(p, 16, 16, 24, 5, 0xFF2A1A20);
                line(p, 16, 22, 26, 17, 0xFF2A1A20);
                line(p, 15, 22, 7, 19, 0xFF2A1A20);
                line(p, 6, 8, 3, 7, 0xFF2A1A20);
                line(p, 24, 5, 27, 4, 0xFF2A1A20);
                line(p, 6, 8, 5, 4, 0xFF2A1A20);
                line(p, 24, 5, 22, 1, 0xFF2A1A20);
                rect(p, 12, 28, 14, 29, 0xFF2A1A20);
                rect(p, 18, 28, 21, 29, 0xFF2A1A20);
                break;
            }

            case World.CAMINHO:
                fill(p, 0xFFDDB77F);
                for (int k = 0; k < 6; k++) circ(p, r.nextInt(T), r.nextInt(T), 3 + r.nextInt(3), 0xFFCFA66C);
                for (int k = 0; k < 4; k++) circ(p, r.nextInt(T), r.nextInt(T), 2 + r.nextInt(2), 0xFFE9C992);
                for (int k = 0; k < 4; k++) {
                    int x = 2 + r.nextInt(27), y = 2 + r.nextInt(27);
                    ell(p, x, y, 2, 1, 0xFFB48A55);
                    set(p, x - 1, y - 1, 0xFFEBD3A6);
                }
                break;

            case World.AREIA:
                fill(p, 0xFFF4E3AA);
                for (int k = 0; k < 5; k++) {
                    int x = r.nextInt(T), y = r.nextInt(T);
                    for (int i = 0; i < 8; i++) set(p, x + i, y + (i % 3 == 0 ? 1 : 0), 0xFFE6D08C);
                }
                speck(p, r, 0xFFFFF3C8, 14);
                speck(p, r, 0xFFD9BE7A, 8);
                if (v == 1) {
                    int x = 6 + r.nextInt(18), y = 6 + r.nextInt(18);
                    set(p, x, y, 0xFFF29AB6); set(p, x + 1, y, 0xFFF29AB6); set(p, x, y + 1, 0xFFD87A98);
                }
                break;

            case World.MONTANHA: {
                fill(p, 0xFF6F6A5C);
                for (int k = 0; k < 6; k++) circ(p, r.nextInt(T), r.nextInt(T), 3, 0xFF625D50);
                int pico = 8 + v * 8;
                for (int y = 3; y < T; y++) {
                    double meia = (y - 3) * 0.62;
                    for (int x = (int) Math.floor(pico - meia); x <= (int) Math.ceil(pico + meia); x++) {
                        boolean esq = x <= pico;
                        double borda = 12 + 2 * Math.sin(x * 0.9);
                        int cor;
                        if (y < borda) {
                            cor = esq ? 0xFFF7FAFF : 0xFFC9D5E8;
                        } else {
                            float f = 1.0f - (y - 12) / 60f;
                            cor = esq ? shade(0xFFB5A898, f) : shade(0xFF6B6055, f);
                        }
                        set(p, x, y, cor);
                    }
                }
                break;
            }

            case World.PISO_CASTELO:
                fill(p, 0xFFD8D3C6);
                for (int by = 0; by < 2; by++) {
                    for (int bx = 0; bx < 2; bx++) {
                        int x0 = bx * 16, y0 = by * 16;
                        rect(p, x0, y0, x0 + 15, y0, 0xFFEFEBE0);
                        rect(p, x0, y0, x0, y0 + 15, 0xFFEFEBE0);
                        rect(p, x0, y0 + 15, x0 + 15, y0 + 15, 0xFF9E998D);
                        rect(p, x0 + 15, y0, x0 + 15, y0 + 15, 0xFF9E998D);
                    }
                }
                speck(p, r, 0xFFC4BFB2, 10);
                break;

            case World.MURO_CASTELO:
                muroBase(p, 0xFF8E8AA6, 0xFF5B5873, 0xFFAAA6C0);
                for (int k = 0; k < 6; k++) set(p, r.nextInt(T), 20 + r.nextInt(11), 0xFF5E9A4A);
                break;

            case World.PAREDE_CASA:
                fill(p, 0xFFEFDDB0);
                speck(p, r, 0xFFE3CE98, 14);
                rect(p, 0, 0, 1, 31, 0xFF6B4423);
                rect(p, 30, 0, 31, 31, 0xFF6B4423);
                rect(p, 0, 28, 31, 31, 0xFF5A3A1E);
                rect(p, 0, 0, 31, 1, 0xFF8A5A33);
                if (v == 1) {
                    rect(p, 7, 6, 24, 22, 0xFF6B4423);
                    rect(p, 9, 8, 22, 20, 0xFF8ED0FF);
                    rect(p, 9, 8, 22, 10, 0xFFBDE6FF);
                    rect(p, 15, 8, 16, 20, 0xFF6B4423);
                    rect(p, 9, 14, 22, 15, 0xFF6B4423);
                    rect(p, 3, 6, 6, 22, 0xFF3F7A4A);
                    rect(p, 25, 6, 28, 22, 0xFF3F7A4A);
                }
                break;

            case World.PORTA:
                fill(p, 0xFFEFDDB0);
                speck(p, r, 0xFFE3CE98, 14);
                rect(p, 0, 0, 1, 31, 0xFF6B4423);
                rect(p, 30, 0, 31, 31, 0xFF6B4423);
                rect(p, 0, 0, 31, 1, 0xFF8A5A33);
                rect(p, 6, 6, 25, 31, 0xFF3F2410);
                rect(p, 8, 8, 23, 31, 0xFF8A5A2E);
                for (int x = 11; x <= 21; x += 4) rect(p, x, 8, x, 31, 0xFF6E4520);
                rect(p, 8, 8, 23, 9, 0xFFA8733C);
                circ(p, 20, 20, 1, 0xFFFFD21E);
                break;

            case World.TELHADO:
                telhado(p, 0xFFC4452F);
                break;

            case World.TELHADO_AZ:
                telhado(p, 0xFF3A66C8);
                break;

            case World.TELHADO_VD:
                telhado(p, 0xFF3F9A55);
                break;

            case World.TERRA_ESCURA:
                terraEscura(p, r);
                break;

            case World.LAVA:
                fill(p, 0xFFE0501A);
                for (int k = 0; k < 6; k++) circ(p, r.nextInt(T), r.nextInt(T), 4, 0xFFB93A12);
                for (int k = 0; k < 5; k++) {
                    int x = (r.nextInt(T) + v * 4) % T, y = (r.nextInt(T) + v * 3) % T;
                    circ(p, x, y, 3, 0xFFFFA52E);
                    circ(p, x, y, 1, 0xFFFFE066);
                }
                for (int k = 0; k < 3; k++) circ(p, r.nextInt(T), r.nextInt(T), 2, 0xFF7A1F10);
                break;

            case World.TERRA_GUERRA:
                guerraBase(p, r);
                break;

            case World.MURO_DEMONIO:
                muroBase(p, 0xFF3A2650, 0xFF1B0F26, 0xFF55386E);
                for (int k = 0; k < 5; k++) {
                    int x = r.nextInt(T);
                    int y = (r.nextInt(4)) * 8 + 7;
                    for (int i = 0; i < 5; i++) set(p, x + i, y, 0xFFC02A2A);
                }
                break;

            case World.PISO_ESCURO:
                fill(p, 0xFF2E2540);
                for (int by = 0; by < 2; by++) {
                    for (int bx = 0; bx < 2; bx++) {
                        int x0 = bx * 16, y0 = by * 16;
                        rect(p, x0, y0, x0 + 15, y0, 0xFF463A5E);
                        rect(p, x0, y0, x0, y0 + 15, 0xFF463A5E);
                        rect(p, x0, y0 + 15, x0 + 15, y0 + 15, 0xFF1D1629);
                        rect(p, x0 + 15, y0, x0 + 15, y0 + 15, 0xFF1D1629);
                    }
                }
                speck(p, r, 0xFF3C3052, 10);
                if (v == 1) {
                    line(p, 12, 8, 20, 8, 0xFFD03A3A);
                    line(p, 16, 4, 16, 12, 0xFFD03A3A);
                    line(p, 12, 4, 20, 12, 0xFF8C2A2A);
                }
                break;

            case World.PEDRA:
                guerraBase(p, r);
                ell(p, 16, 26, 13, 4, 0xFF6E5A42);
                ell(p, 16, 19, 12, 9, 0xFF6A6A6A);
                ell(p, 15, 17, 11, 8, 0xFF9A9A9A);
                ell(p, 12, 13, 6, 4, 0xFFC8C8C8);
                line(p, 20, 14, 23, 20, 0xFF6A6A6A);
                circ(p, 22, 24, 2, 0xFF7E8A5A);
                circ(p, 8, 22, 2, 0xFF7E8A5A);
                break;

            case World.TAPETE:
                fill(p, 0xFFB8232F);
                rect(p, 0, 0, 31, 1, 0xFFE6B422);
                rect(p, 0, 30, 31, 31, 0xFFE6B422);
                rect(p, 0, 0, 1, 31, 0xFFE6B422);
                rect(p, 30, 0, 31, 31, 0xFFE6B422);
                rect(p, 3, 3, 28, 28, 0xFF8E1823);
                rect(p, 4, 4, 27, 27, 0xFFB8232F);
                for (int k = 0; k < 3; k++) {
                    int c = 8 + k * 8;
                    line(p, 16, c - 4, 20, c, 0xFFE6B422);
                    line(p, 20, c, 16, c + 4, 0xFFE6B422);
                    line(p, 16, c + 4, 12, c, 0xFFE6B422);
                    line(p, 12, c, 16, c - 4, 0xFFE6B422);
                }
                break;

            default:
                fill(p, 0xFFFF00FF);
        }
        return p;
    }

    // ---------------------------------------------------------------- silhueta branca (efeito de dano)

    static Bitmap branco(Bitmap src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int[] p = new int[w * h];
        src.getPixels(p, 0, w, 0, 0, w, h);
        for (int i = 0; i < p.length; i++) {
            if ((p[i] >>> 24) != 0) p[i] = 0xFFFFFFFF;
        }
        return bitmap(p, w, h);
    }
}
