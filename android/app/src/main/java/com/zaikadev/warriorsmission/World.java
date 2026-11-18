package com.zaikadev.warriorsmission;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * O mundo aberto: mapa de 144x144 tiles gerado a partir do desenho da ilha,
 * pessoas (Npc) e monstros que andam sozinhos (Inimigo).
 * Cada tile tem 32x32 pixels.
 */
public class World {

    public static final int W = 144;
    public static final int H = 144;
    public static final int TILE = 32;
    public static final long PASSO_MONSTRO = 450L;
    public static final int MAX_INIMIGOS = 56;

    // tipos de tile
    public static final int AGUA = 0, GRAMA = 1, ARVORE = 2, CAMINHO = 3, AREIA = 4,
            MONTANHA = 5, PISO_CASTELO = 6, MURO_CASTELO = 7, PAREDE_CASA = 8,
            TERRA_ESCURA = 9, LAVA = 10, TERRA_GUERRA = 11, FLOR = 12, TELHADO = 13,
            PORTA = 14, MURO_DEMONIO = 15, PISO_ESCURO = 16, PEDRA = 17, PINHEIRO = 18,
            GRAMA_ALTA = 19, TELHADO_AZ = 20, TELHADO_VD = 21, TAPETE = 22, ARVORE_MORTA = 23;
    public static final int NUM_TILES = 24;

    public static class Area {
        public final String nome;
        public final int x0, y0, x1, y1;
        Area(String nome, int x0, int y0, int x1, int y1) {
            this.nome = nome;
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
        }
        public boolean contains(int x, int y) {
            return x >= x0 && x <= x1 && y >= y0 && y <= y1;
        }
    }

    private static class Respawn {
        final Especie especie;
        long quando;
        Respawn(Especie e, long q) {
            especie = e;
            quando = q;
        }
    }

    public final int[][] tiles = new int[H][W];
    public final List<Npc> npcs = new ArrayList<Npc>();
    public final List<Inimigo> inimigos = new ArrayList<Inimigo>();
    public int startX, startY;
    public Area reino, vila, guerra, demonio, passagem;

    private final boolean[][] reservado = new boolean[H][W];
    private final boolean[][] margemEstrada = new boolean[H][W];
    private final List<Respawn> respawns = new ArrayList<Respawn>();
    private final Random rnd = new Random();
    private int proximoId = 0;

    // coordenadas do desenho original (550x550)
    private static final double ESC = 550.0 / 144.0;

    private static final double[][] ILHA = {
            {105, 74}, {180, 68}, {260, 78}, {295, 88}, {335, 90}, {430, 100}, {475, 128},
            {492, 178}, {472, 255}, {448, 312}, {400, 360}, {352, 380}, {336, 405}, {332, 445},
            {322, 492}, {292, 510}, {240, 507}, {205, 478}, {178, 425}, {166, 385}, {176, 335},
            {172, 285}, {168, 250}, {110, 246}, {78, 226}, {64, 186}, {68, 135}, {86, 95}
    };

    private static final double[][] MONT = {
            {290, 88}, {330, 88}, {335, 200}, {352, 280}, {385, 330}, {380, 372},
            {335, 378}, {318, 340}, {300, 280}, {292, 200}
    };

    /** Converte uma coordenada do desenho (0..550) para tiles. */
    private static int T(double d) {
        return (int) Math.round(d * W / 550.0);
    }

    /** Converte uma coordenada do mapa antigo (96x96) para o mapa atual. */
    private static int S(int n) {
        return (int) Math.round(n * W / 96.0);
    }

    public World() {
        gerar();
    }

    // ------------------------------------------------------------ consultas

    public static boolean solido(int id) {
        switch (id) {
            case AGUA:
            case ARVORE:
            case MONTANHA:
            case MURO_CASTELO:
            case PAREDE_CASA:
            case LAVA:
            case TELHADO:
            case TELHADO_AZ:
            case TELHADO_VD:
            case PORTA:
            case MURO_DEMONIO:
            case PEDRA:
            case PINHEIRO:
            case ARVORE_MORTA:
                return true;
            default:
                return false;
        }
    }

    public boolean isSolid(int x, int y) {
        if (x < 0 || y < 0 || x >= W || y >= H) return true;
        return solido(tiles[y][x]);
    }

    public int variante(int x, int y) {
        return ((x * 7 + y * 13 + x * y) & 0x7fffffff) % 3;
    }

    public Npc npcAt(int x, int y) {
        for (Npc n : npcs) {
            if (n.x == x && n.y == y) return n;
        }
        return null;
    }

    public Inimigo inimigoAt(int x, int y) {
        for (Inimigo m : inimigos) {
            if (m.x == x && m.y == y) return m;
        }
        return null;
    }

    /** Nome do lugar onde o herói está (ou null). */
    public String regiaoNome(int x, int y) {
        if (passagem.contains(x, y)) return passagem.nome;
        if (vila.contains(x, y)) return vila.nome;
        if (reino.contains(x, y)) return reino.nome;
        if (demonio.contains(x, y)) return demonio.nome;
        if (guerra.contains(x, y)) return guerra.nome;
        return null;
    }

    /** Qual monstro vive nessa posição (null = zona segura). */
    public Especie especieAt(int x, int y) {
        if (vila.contains(x, y) || reino.contains(x, y)) return null;
        if (x >= S(62)) return Especie.SOLDADO;
        if (guerra.contains(x, y)) return Especie.GOBLIN;
        return Especie.SLIME;
    }

    private boolean tileDeMonstro(int id) {
        return id == GRAMA || id == FLOR || id == AREIA || id == TERRA_GUERRA
                || id == TERRA_ESCURA || id == CAMINHO || id == GRAMA_ALTA;
    }

    private boolean monstroPodeFicar(Especie e, int x, int y) {
        if (x < 0 || y < 0 || x >= W || y >= H) return false;
        if (!tileDeMonstro(tiles[y][x])) return false;
        return especieAt(x, y) == e;
    }

    // ------------------------------------------------------------ monstros

    public void popular(int heroX, int heroY) {
        inimigos.clear();
        respawns.clear();
        proximoId = 0;

        Inimigo rei = new Inimigo(proximoId++, Especie.REI, S(72), S(25));
        rei.fixo = true;
        inimigos.add(rei);

        Especie[] tipos = {Especie.SLIME, Especie.GOBLIN, Especie.SOLDADO};
        int[] qtd = {21, 15, 18};
        for (int i = 0; i < tipos.length; i++) {
            for (int k = 0; k < qtd[i]; k++) {
                gerarInimigo(tipos[i], heroX, heroY);
            }
        }
    }

    private boolean gerarInimigo(Especie e, int heroX, int heroY) {
        for (int tentativa = 0; tentativa < 160; tentativa++) {
            int x = rnd.nextInt(W);
            int y = rnd.nextInt(H);
            if (!monstroPodeFicar(e, x, y)) continue;
            if (Math.abs(x - heroX) + Math.abs(y - heroY) < 12) continue;
            if (inimigoAt(x, y) != null || npcAt(x, y) != null) continue;
            inimigos.add(new Inimigo(proximoId++, e, x, y));
            return true;
        }
        return false;
    }

    /** Remove o monstro derrotado; ele reaparece depois (menos o chefe). */
    public void derrotar(Inimigo m, long agora) {
        inimigos.remove(m);
        if (!m.especie.chefe) {
            respawns.add(new Respawn(m.especie, agora + 45000L));
        }
    }

    /** Deixa o monstro parado por um tempo (usado quando o herói foge). */
    public void atordoar(Inimigo m, long agora, long ms) {
        m.nextAction = agora + ms;
    }

    /**
     * Anda com os monstros. Devolve um monstro que encostou no herói (ou null).
     */
    public Inimigo update(long agora, int heroX, int heroY, boolean heroInvulneravel) {
        Inimigo tocou = null;
        for (Inimigo m : inimigos) {
            if (m.moving && agora - m.moveStart >= PASSO_MONSTRO) m.moving = false;
            if (m.fixo || m.moving || agora < m.nextAction) continue;

            m.nextAction = agora + 600 + rnd.nextInt(1400);
            if (rnd.nextInt(100) < 40) continue; // fica parado

            int dir = rnd.nextInt(4);
            int nx = m.x + (dir == 2 ? -1 : dir == 3 ? 1 : 0);
            int ny = m.y + (dir == 0 ? 1 : dir == 1 ? -1 : 0);

            if (nx == heroX && ny == heroY) {
                if (!heroInvulneravel && tocou == null) tocou = m;
                continue;
            }
            if (!podeAndar(m, nx, ny, heroX, heroY)) continue;

            m.fromX = m.x;
            m.fromY = m.y;
            m.x = nx;
            m.y = ny;
            m.moving = true;
            m.moveStart = agora;
            m.nextAction = agora + PASSO_MONSTRO + 600 + rnd.nextInt(1400);
        }

        // reaparecer monstros derrotados
        for (int i = respawns.size() - 1; i >= 0; i--) {
            Respawn r = respawns.get(i);
            if (agora < r.quando) continue;
            if (inimigos.size() < MAX_INIMIGOS && gerarInimigo(r.especie, heroX, heroY)) {
                respawns.remove(i);
            } else {
                r.quando = agora + 5000L;
            }
        }
        return tocou;
    }

    private boolean podeAndar(Inimigo m, int nx, int ny, int heroX, int heroY) {
        if (Math.abs(nx - m.homeX) > 5 || Math.abs(ny - m.homeY) > 5) return false;
        if (!monstroPodeFicar(m.especie, nx, ny)) return false;
        if (nx == heroX && ny == heroY) return false;
        if (npcAt(nx, ny) != null) return false;
        return inimigoAt(nx, ny) == null;
    }

    // ------------------------------------------------------------ geração do mapa

    private static double hash(int x, int y, int seed) {
        int h = x * 374761393 + y * 668265263 + seed * 1442695041;
        h = (h ^ (h >>> 13)) * 1274126177;
        h = h ^ (h >>> 16);
        return (h & 0xffff) / 65535.0;
    }

    private static double vnoise(double x, double y, int seed) {
        int xi = (int) Math.floor(x);
        int yi = (int) Math.floor(y);
        double xf = x - xi;
        double yf = y - yi;
        double u = xf * xf * (3 - 2 * xf);
        double v = yf * yf * (3 - 2 * yf);
        double a = hash(xi, yi, seed);
        double b = hash(xi + 1, yi, seed);
        double c = hash(xi, yi + 1, seed);
        double d = hash(xi + 1, yi + 1, seed);
        return a * (1 - u) * (1 - v) + b * u * (1 - v) + c * (1 - u) * v + d * u * v;
    }

    private static boolean dentro(double[][] poly, double px, double py) {
        boolean in = false;
        for (int i = 0, j = poly.length - 1; i < poly.length; j = i++) {
            double xi = poly[i][0], yi = poly[i][1];
            double xj = poly[j][0], yj = poly[j][1];
            if (((yi > py) != (yj > py)) && (px < (xj - xi) * (py - yi) / (yj - yi) + xi)) {
                in = !in;
            }
        }
        return in;
    }

    private boolean ehTerreno(int id) {
        return id == GRAMA || id == AREIA || id == FLOR || id == ARVORE
                || id == TERRA_GUERRA || id == TERRA_ESCURA || id == PEDRA;
    }

    private void gerar() {
        // 1) ilha
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                double px = (x + 0.5) * ESC;
                double py = (y + 0.5) * ESC;
                double ox = (vnoise(x / 7.5, y / 7.5, 1) - 0.5) * 34;
                double oy = (vnoise(x / 7.5 + 40, y / 7.5 + 40, 2) - 0.5) * 34;
                tiles[y][x] = dentro(ILHA, px + ox, py + oy) ? GRAMA : AGUA;
            }
        }

        // 2) areia na beira do mar
        int[][] copia = new int[H][W];
        for (int y = 0; y < H; y++) System.arraycopy(tiles[y], 0, copia[y], 0, W);
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                if (copia[y][x] != GRAMA) continue;
                boolean perto = false;
                if (x > 0 && copia[y][x - 1] == AGUA) perto = true;
                if (x < W - 1 && copia[y][x + 1] == AGUA) perto = true;
                if (y > 0 && copia[y - 1][x] == AGUA) perto = true;
                if (y < H - 1 && copia[y + 1][x] == AGUA) perto = true;
                if (perto) tiles[y][x] = AREIA;
            }
        }

        // 3) montanha
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                if (tiles[y][x] == AGUA) continue;
                double px = (x + 0.5) * ESC;
                double py = (y + 0.5) * ESC;
                double ox = (vnoise(x / 6.0, y / 6.0, 3) - 0.5) * 16;
                double oy = (vnoise(x / 6.0 + 20, y / 6.0 + 20, 4) - 0.5) * 8;
                if (dentro(MONT, px + ox, py + oy)) tiles[y][x] = MONTANHA;
            }
        }

        // áreas (em tiles)
        reino = new Area("Reino e Castelo", T(90), T(105), T(200), T(220));
        demonio = new Area("Reino Demônio", T(365), T(125), T(455), T(260));
        vila = new Area("Vila Inicial", T(200), T(400), T(280), T(470));
        guerra = new Area("Área de Guerra", T(172), T(262), T(292), T(332));
        passagem = new Area("Passagem da Montanha", T(290), T(292), T(380), T(306));

        // 4) passagem pela montanha
        for (int y = passagem.y0; y <= passagem.y1; y++) {
            for (int x = passagem.x0; x <= passagem.x1; x++) {
                if (tiles[y][x] == MONTANHA) tiles[y][x] = CAMINHO;
            }
        }

        // 5) Área de Guerra
        for (int y = guerra.y0; y <= guerra.y1; y++) {
            for (int x = guerra.x0; x <= guerra.x1; x++) {
                if (ehTerreno(tiles[y][x])) {
                    tiles[y][x] = hash(x, y, 21) < 0.05 ? PEDRA : TERRA_GUERRA;
                }
            }
        }
        reservar(guerra, 0);

        montarReino();
        montarDemonio();
        montarVila();

        // 6) estradas
        estrada(new int[][]{{S(41), S(82)}, {S(41), S(50)}, {S(33), S(47)}, {S(28), S(42)},
                {S(25), S(38)}, {S(25), S(28)}});
        estrada(new int[][]{{S(41), S(52)}, {S(66), S(52)}, {S(70), S(48)}, {S(72), S(44)},
                {S(72), S(32)}});
        estrada(new int[][]{{S(17), S(33)}, {S(34), S(33)}});
        estrada(new int[][]{{S(36), S(76)}, {S(48), S(76)}});

        startX = S(42);
        startY = S(77);
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) reservado[startY + dy][startX + dx] = true;
        }

        // 7) lagos
        criarLagos();

        // 8) árvores, flores e capim
        enfeitar();

        // 9) tapete do castelo
        int portao = reino.x0 + 14;
        for (int y = reino.y0 + 5; y <= reino.y0 + 14; y++) {
            tiles[y][portao] = TAPETE;
            tiles[y][portao + 1] = TAPETE;
        }

        criarNpcs();
    }

    private void reservar(Area a, int margem) {
        for (int y = Math.max(0, a.y0 - margem); y <= Math.min(H - 1, a.y1 + margem); y++) {
            for (int x = Math.max(0, a.x0 - margem); x <= Math.min(W - 1, a.x1 + margem); x++) {
                reservado[y][x] = true;
            }
        }
    }

    private void estrada(int[][] pts) {
        for (int i = 0; i + 1 < pts.length; i++) {
            int x0 = pts[i][0], y0 = pts[i][1], x1 = pts[i + 1][0], y1 = pts[i + 1][1];
            int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
            int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
            int err = dx - dy;
            int x = x0, y = y0;
            while (true) {
                for (int ax = 0; ax <= 1; ax++) {
                    for (int ay = 0; ay <= 1; ay++) {
                        cavar(x + ax, y + ay);
                    }
                }
                if (x == x1 && y == y1) break;
                int e2 = 2 * err;
                if (e2 > -dy) {
                    err -= dy;
                    x += sx;
                }
                if (e2 < dx) {
                    err += dx;
                    y += sy;
                }
            }
        }
    }

    private void cavar(int x, int y) {
        if (x < 0 || y < 0 || x >= W || y >= H) return;
        int id = tiles[y][x];
        if (id == GRAMA || id == AREIA || id == FLOR || id == ARVORE || id == TERRA_GUERRA
                || id == TERRA_ESCURA || id == PEDRA) {
            tiles[y][x] = CAMINHO;
        }
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int nx = x + dx, ny = y + dy;
                if (nx >= 0 && ny >= 0 && nx < W && ny < H) {
                    reservado[ny][nx] = true;
                    margemEstrada[ny][nx] = true;
                }
            }
        }
    }

    /** cor: 0 = telhado vermelho, 1 = azul, 2 = verde */
    private void casa(int x, int y, int larg, int cor) {
        int telhado = cor == 0 ? TELHADO : (cor == 1 ? TELHADO_AZ : TELHADO_VD);
        for (int i = 0; i < larg; i++) {
            tiles[y][x + i] = telhado;
            tiles[y + 1][x + i] = telhado;
            tiles[y + 2][x + i] = PAREDE_CASA;
        }
        tiles[y + 2][x + larg / 2] = PORTA;
    }

    private void limpar(Area a, int tipo) {
        for (int y = a.y0; y <= a.y1; y++) {
            for (int x = a.x0; x <= a.x1; x++) {
                if (tiles[y][x] != AGUA && tiles[y][x] != MONTANHA) tiles[y][x] = tipo;
            }
        }
    }

    private void montarReino() {
        limpar(reino, GRAMA);
        reservar(reino, 0);
        int cx0 = reino.x0 + 8, cx1 = reino.x0 + 21;
        int cy0 = reino.y0 + 2, cy1 = reino.y0 + 14;
        for (int y = cy0; y <= cy1; y++) {
            for (int x = cx0; x <= cx1; x++) {
                boolean borda = (x == cx0 || x == cx1 || y == cy0 || y == cy1);
                tiles[y][x] = borda ? MURO_CASTELO : PISO_CASTELO;
            }
        }
        int portao = reino.x0 + 14;
        tiles[cy1][portao] = PISO_CASTELO;
        tiles[cy1][portao + 1] = PISO_CASTELO;
        casa(reino.x0 + 2, reino.y0 + 18, 7, 1);
        casa(reino.x0 + 21, reino.y0 + 18, 7, 0);
        casa(reino.x0 + 21, reino.y0 + 26, 7, 2);
    }

    private void montarDemonio() {
        limpar(demonio, TERRA_ESCURA);
        reservar(demonio, 0);
        int cx0 = demonio.x0 + 5, cx1 = demonio.x0 + 20;
        int cy0 = demonio.y0, cy1 = demonio.y0 + 14;
        for (int y = cy0; y <= cy1; y++) {
            for (int x = cx0; x <= cx1; x++) {
                if (tiles[y][x] == AGUA) continue;
                boolean borda = (x == cx0 || x == cx1 || y == cy0 || y == cy1);
                tiles[y][x] = borda ? MURO_DEMONIO : PISO_ESCURO;
            }
        }
        int portao = demonio.x0 + 12;
        tiles[cy1][portao] = PISO_ESCURO;
        tiles[cy1][portao + 1] = PISO_ESCURO;
        lava(demonio.x0 + 3, demonio.y0 + 24, 3);
        lava(demonio.x1 - 3, demonio.y0 + 29, 3);
        lava(demonio.x0 + 8, demonio.y0 + 32, 3);
    }

    private void lava(int cx, int cy, int r) {
        for (int y = cy - r; y <= cy + r; y++) {
            for (int x = cx - r; x <= cx + r; x++) {
                if (x < 0 || y < 0 || x >= W || y >= H) continue;
                if ((x - cx) * (x - cx) + (y - cy) * (y - cy) <= r * r + 1
                        && tiles[y][x] == TERRA_ESCURA) {
                    tiles[y][x] = LAVA;
                }
            }
        }
    }

    private void montarVila() {
        limpar(vila, GRAMA);
        reservar(vila, 0);
        casa(S(36), S(71), 7, 0);
        casa(S(44), S(71), 7, 1);
        casa(S(36), S(78), 7, 2);
        casa(S(44), S(78), 7, 0);
    }

    /** Lagos no meio da grama. */
    private void criarLagos() {
        Random lr = new Random(4242);
        int feitos = 0;
        for (int tentativa = 0; tentativa < 400 && feitos < 7; tentativa++) {
            int cx = 8 + lr.nextInt(W - 16);
            int cy = 8 + lr.nextInt(H - 16);
            int r = 3 + lr.nextInt(3);
            boolean ok = true;
            for (int y = cy - r - 3; y <= cy + r + 3 && ok; y++) {
                for (int x = cx - r - 3; x <= cx + r + 3; x++) {
                    if (x < 0 || y < 0 || x >= W || y >= H) { ok = false; break; }
                    int id = tiles[y][x];
                    if (reservado[y][x] || (id != GRAMA && id != FLOR)) { ok = false; break; }
                }
            }
            if (!ok) continue;
            for (int y = cy - r - 1; y <= cy + r + 1; y++) {
                for (int x = cx - r - 1; x <= cx + r + 1; x++) {
                    double d = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
                    double lim = r + (vnoise(x / 2.5, y / 2.5, 31) - 0.5) * 2.2;
                    if (d <= lim) tiles[y][x] = AGUA;
                }
            }
            for (int y = cy - r - 3; y <= cy + r + 3; y++) {
                for (int x = cx - r - 3; x <= cx + r + 3; x++) {
                    if (tiles[y][x] != GRAMA) continue;
                    if (tiles[y][x - 1] == AGUA || tiles[y][x + 1] == AGUA
                            || tiles[y - 1][x] == AGUA || tiles[y + 1][x] == AGUA) {
                        tiles[y][x] = AREIA;
                    }
                }
            }
            for (int y = cy - r - 3; y <= cy + r + 3; y++) {
                for (int x = cx - r - 3; x <= cx + r + 3; x++) reservado[y][x] = true;
            }
            feitos++;
        }
    }

    private void enfeitar() {
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                int id = tiles[y][x];
                if (id == TERRA_ESCURA && !reservadoCastelo(x, y) && !margemEstrada[y][x]
                        && hash(x, y, 17) < 0.05) {
                    tiles[y][x] = ARVORE_MORTA;
                    continue;
                }
                if (id != GRAMA || reservado[y][x]) continue;
                if (Math.abs(x - startX) + Math.abs(y - startY) < 9) continue;
                double f = vnoise(x / 6.5, y / 6.5, 7);
                if (f > 0.6 && hash(x, y, 9) < 0.78) {
                    boolean faixaMontanha = Math.abs(x - 92) < 24 && y < 100;
                    boolean pinheiro = faixaMontanha ? hash(x, y, 13) < 0.8 : hash(x, y, 13) < 0.15;
                    tiles[y][x] = pinheiro ? PINHEIRO : ARVORE;
                } else if (vnoise(x / 5.0, y / 5.0, 15) > 0.68 && hash(x, y, 19) < 0.55) {
                    tiles[y][x] = GRAMA_ALTA;
                } else if (hash(x, y, 11) < 0.07) {
                    tiles[y][x] = FLOR;
                }
            }
        }
    }

    private boolean reservadoCastelo(int x, int y) {
        int cx0 = demonio.x0 + 5, cx1 = demonio.x0 + 20;
        return x >= cx0 - 1 && x <= cx1 + 1 && y >= demonio.y0 && y <= demonio.y0 + 15;
    }

    // ------------------------------------------------------------ pessoas (edite as falas aqui!)

    private void addNpc(Npc n) {
        if (solido(tiles[n.y][n.x])) tiles[n.y][n.x] = GRAMA;
        npcs.add(n);
    }

    private void criarNpcs() {
        // Vila Inicial
        addNpc(new Npc(S(39), S(75), "Velho", "velho", false,
                "Velho: Bem-vindo, jovem. Dizem que um garoto sobreviveu ao ataque ao reino de Vergiy.",
                "Velho: Siga a estrada ao norte até a Área de Guerra. Mas cuidado com os monstros!"));
        addNpc(new Npc(S(45), S(75), "Curandeira", "curandeira", true,
                "Curandeira: Você parece cansado. Deixe-me cuidar dos seus ferimentos.",
                "Curandeira: Pronto! Seu HP e seu MP foram restaurados."));
        addNpc(new Npc(S(43), S(82), "Criança", "crianca", false,
                "Criança: Eu vi um monstro enorme no castelo escuro, lá no leste!"));

        // Área de Guerra
        addNpc(new Npc(S(39), S(54), "Soldado", "soldado", false,
                "Soldado: Esta é a Área de Guerra. Os demônios vêm do leste, pela passagem da montanha.",
                "Soldado: Fique forte antes de atravessar!"));

        // Reino e Castelo
        int portao = reino.x0 + 14;
        addNpc(new Npc(portao - 2, reino.y0 + 15, "Guarda", "guarda", false,
                "Guarda: O rei espera por você no castelo."));
        addNpc(new Npc(portao + 3, reino.y0 + 15, "Guarda", "guarda", false,
                "Guarda: O Reino Demônio fica além da montanha, a leste."));
        addNpc(new Npc(portao, reino.y0 + 7, "Rei", "rei", false,
                "Rei: Então você é o garoto que sobreviveu...",
                "Rei: Derrote o Rei Monstro, no Reino Demônio, e traga paz ao nosso povo.",
                "Rei: Que a sua missão tenha sucesso, herói!"));
        addNpc(new Npc(S(21), S(35), "Aldeã", "alda", false,
                "Aldeã: Os monstros ficam mais fortes perto do Reino Demônio."));
        addNpc(new Npc(S(28), S(37), "Aldeão", "aldeao", false,
                "Aldeão: Se estiver ferido, volte à Vila Inicial. A curandeira ajuda."));
    }
}
