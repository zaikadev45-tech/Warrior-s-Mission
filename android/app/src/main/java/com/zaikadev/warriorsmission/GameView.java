package com.zaikadev.warriorsmission;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Tela do jogo em pixel art estilo "Dragon Quest": mundo aberto + batalhas.
 * Desenha numa tela virtual de 320x180 e escala para o celular.
 */
public class GameView extends View {

    private static final float W = 320f;
    private static final float H = 180f;
    private static final int TILE = World.TILE;       // 32 pixels
    private static final int WORLD_W = 640, WORLD_H = 360;   // o mundo é desenhado em 640x360
    private static final int HERO_W = 64, HERO_H = 64;       // tamanho de cada quadro do herói
    private static final int HERO_FEET = 58;                 // linha dos pés dentro do quadro
    private static final int HERO_CENTER_X = 29;             // centro do corpo dentro do quadro

    // estados da tela
    private static final int TITLE = 0, ASK_SKIP = 1, MESSAGE = 2, MENU = 3, END = 4, WORLD = 5;
    // o que aparece atrás das janelas
    private static final int SCENE_BLACK = 0, SCENE_WORLD = 1, SCENE_BATTLE = 2;
    // passos da fila de mensagens
    private static final int S_TEXT = 0, S_PLAYER_ATTACK = 1, S_MONSTER_ATTACK = 2,
            S_HEAL = 3, S_FLEE = 4, S_REWARD = 5;
    // o que fazer quando as mensagens acabarem
    private static final int AFTER_MENU = 0, AFTER_START = 1, AFTER_END = 2,
            AFTER_WORLD = 3, AFTER_RESPAWN = 4;

    private static final int HEAL_COST = 8;
    private static final int MAX_LINES = 4;
    private static final float TEXT_X = 18f;
    private static final float TEXT_W = 284f;

    // janelas (coordenadas da tela virtual)
    private static final float MSG_X = 8f, MSG_Y = 112f, MSG_W = 304f, MSG_H = 60f;
    private static final float CMD_X = 8f, CMD_Y = 54f, CMD_W = 96f;
    private static final float ROW_H = 15f;

    // controle na tela (setas)
    private static final float PAD_X = 42f, PAD_Y = 140f, PAD_R = 52f;

    // tela inicial: animação da capa (região e quadros gerados a partir da arte)
    private static final int CAPE_X = 18, CAPE_Y = 80, CAPE_W = 54, CAPE_H = 72;
    private static final int CAPE_FRAMES = 20;
    private static final long CAPE_CYCLE = 3600L;  // tempo total de um ciclo (ms)
    private static final long CAPE_GUST = 2000L;   // tempo em que o vento sopra (ms)
    // botões desenhados na imagem da tela inicial: {topo, base} em pixels da tela virtual
    private static final int[][] TITLE_BTN = {{96, 114}, {115, 131}, {132, 148}, {149, 165}};
    private static final float TITLE_BTN_X0 = 114f, TITLE_BTN_X1 = 206f;

    // música da tela inicial: depois da 1ª vez, volta para este ponto (ms) em vez de ir ao começo
    private static final int MUSIC_LOOP_START_MS = 3000;

    // movimento do herói
    private static final long HERO_STEP = 210L;
    private static final int[] DX = {0, 0, -1, 1};      // 0=baixo 1=cima 2=esquerda 3=direita
    private static final int[] DY = {1, -1, 0, 0};
    private static final int[] FRAME_DIR = {3, 1, 0, 2}; // quadro do sprite para cada direção

    private static class Step {
        final int type;
        final String text;
        Step(int type, String text) {
            this.type = type;
            this.text = text;
        }
    }

    private final Paint fill = new Paint();
    private final Paint text = new Paint();
    private final Paint title = new Paint();
    private final Random rnd = new Random();

    private final ArrayDeque<Step> queue = new ArrayDeque<Step>();
    private List<String> msgLines = new ArrayList<String>();
    private long msgStart = 0;

    private int state = TITLE;
    private int scene = SCENE_BLACK;
    private int after = AFTER_MENU;
    private int menuIndex = 0;

    // gráficos
    private Bitmap[][] tiles;
    private Bitmap[] heroFrames = new Bitmap[4];
    private Bitmap titleBg;
    private Bitmap[] capeFrames = new Bitmap[CAPE_FRAMES];
    private MediaPlayer musica;
    private boolean querMusica = false;
    private boolean musicaSaindo = false;
    private float musicaVol = 1f;
    private String titleNotice = null;
    private long titleNoticeUntil = 0;
    private final Map<Especie, Bitmap> bmpEspecies = new HashMap<Especie, Bitmap>();
    private final Map<Especie, Bitmap> bmpBrancos = new HashMap<Especie, Bitmap>();
    private final Map<Npc, Bitmap> bmpNpcs = new HashMap<Npc, Bitmap>();

    // jogo
    private World world;
    private Player player;
    private Monster battleMonster;
    private Inimigo battleFoe;

    // herói no mapa
    private int heroX, heroY, heroFromX, heroFromY;
    private int heroFacing = 0;
    private boolean heroMoving = false;
    private long heroMoveStart = 0;
    private long invulnUntil = 0;
    private int heldDir = -1;
    private boolean holdValid = false;

    private String regiaoAtual = null;
    private String bannerText = null;
    private long bannerUntil = 0;

    private long shakeUntil = 0;
    private long monsterFlashUntil = 0;

    private float scale = 1f, offX = 0f, offY = 0f;

    public GameView(Context context) {
        super(context);
        fill.setAntiAlias(false);
        fill.setStyle(Paint.Style.FILL);

        text.setTypeface(Typeface.MONOSPACE);
        text.setTextSize(11f);
        text.setColor(Color.WHITE);
        text.setAntiAlias(true);

        title.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        title.setTextAlign(Paint.Align.CENTER);
        title.setAntiAlias(true);

        tiles = Sprites.tiles();
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;
        Bitmap sheet = BitmapFactory.decodeResource(getResources(), R.drawable.hero_sheet, opts);
        for (int i = 0; i < 4; i++) {
            heroFrames[i] = Bitmap.createBitmap(sheet, i * HERO_W, 0, HERO_W, HERO_H);
        }

        titleBg = BitmapFactory.decodeResource(getResources(), R.drawable.title_bg, opts);
        Bitmap capa = BitmapFactory.decodeResource(getResources(), R.drawable.title_cape, opts);
        for (int i = 0; i < CAPE_FRAMES; i++) {
            capeFrames[i] = Bitmap.createBitmap(capa, i * CAPE_W, 0, CAPE_W, CAPE_H);
        }

        novoJogo();
        iniciarMusica();
    }

    // ------------------------------------------------------------ música

    private void iniciarMusica() {
        try {
            if (musica == null) {
                musica = MediaPlayer.create(getContext(), R.raw.musica_titulo);
                if (musica == null) return;
                musica.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (querMusica) {
                            mp.seekTo(MUSIC_LOOP_START_MS);
                            mp.start();
                        }
                    }
                });
            }
            musicaSaindo = false;
            musicaVol = 1f;
            musica.setVolume(1f, 1f);
            querMusica = true;
            if (!musica.isPlaying()) {
                musica.seekTo(0);
                musica.start();
            }
        } catch (Exception e) {
            // sem música o jogo continua normalmente
        }
    }

    /** Diminui o volume até parar (usado quando o jogo começa). */
    private void pararMusicaSuave() {
        if (musica != null && querMusica) musicaSaindo = true;
    }

    private void atualizarMusica() {
        if (musica == null || !musicaSaindo) return;
        try {
            musicaVol -= 0.035f;
            if (musicaVol <= 0f) {
                musica.pause();
                musica.seekTo(0);
                musicaSaindo = false;
                querMusica = false;
                musicaVol = 1f;
            } else {
                musica.setVolume(musicaVol, musicaVol);
            }
        } catch (Exception e) {
            // ignora
        }
    }

    public void pausarMusica() {
        try {
            if (musica != null && musica.isPlaying()) musica.pause();
        } catch (Exception e) {
            // ignora
        }
    }

    public void retomarMusica() {
        try {
            if (musica != null && querMusica && !musica.isPlaying()) musica.start();
        } catch (Exception e) {
            // ignora
        }
    }

    public void liberarMusica() {
        try {
            if (musica != null) musica.release();
        } catch (Exception e) {
            // ignora
        }
        musica = null;
        querMusica = false;
    }

    // ------------------------------------------------------------ lógica do jogo

    private void novoJogo() {
        player = new Player("Heroi");
        world = new World();
        heroX = world.startX;
        heroY = world.startY;
        heroFromX = heroX;
        heroFromY = heroY;
        heroMoving = false;
        heroFacing = 0;
        invulnUntil = 0;
        heldDir = -1;
        regiaoAtual = null;
        bannerText = null;
        battleFoe = null;
        battleMonster = null;
        queue.clear();
        msgLines = new ArrayList<String>();
        monsterFlashUntil = 0;
        shakeUntil = 0;
        bmpNpcs.clear();
    }

    private void startIntro() {
        queue.clear();
        queue.add(new Step(S_TEXT, "Em um continente distante havia um reino chamado \u201cVergiy\u201d foi "
                + "aonde um garoto que futuramente o chamariam de \u201c O her\u00f3i \u201d."));
        queue.add(new Step(S_TEXT, "Ap\u00f3s o nascimento do garoto o reino foi atacado por um ex\u00e9rcito "
                + "dem\u00f4nio, mais o garoto foi levanto por um pequeno barco antes do ataque no reino "
                + "e foi levando pra uma pequena ilha..."));
        queue.add(new Step(S_TEXT, "Anos ap\u00f3s ele cresceu e ficou forte e tinha o objetivo \u00fanico de "
                + "saber aonde ele nasceu e quem"));
        after = AFTER_START;
        scene = SCENE_BLACK;
        advance();
    }

    private void startWorld() {
        pararMusicaSuave();
        novoJogo();
        world.popular(heroX, heroY);
        scene = SCENE_WORLD;
        atualizarRegiao(SystemClock.uptimeMillis());
        queue.add(new Step(S_TEXT, "Use as setas na tela para andar."));
        queue.add(new Step(S_TEXT, "Encoste nos monstros para lutar e converse com as pessoas!"));
        after = AFTER_WORLD;
        advance();
    }

    private void atualizarRegiao(long now) {
        String nome = world.regiaoNome(heroX, heroY);
        if (nome != null && !nome.equals(regiaoAtual)) {
            bannerText = nome;
            bannerUntil = now + 2500L;
        }
        regiaoAtual = nome;
    }

    private void talk(Npc n) {
        heldDir = -1;
        queue.clear();
        for (String fala : n.falas) {
            queue.add(new Step(S_TEXT, fala));
        }
        if (n.cura) player.restaurarTudo();
        after = AFTER_WORLD;
        advance();
    }

    private void startBattle(Inimigo foe) {
        heldDir = -1;
        battleFoe = foe;
        battleMonster = new Monster(foe.especie);
        scene = SCENE_BATTLE;
        queue.clear();
        if (foe.especie.chefe) {
            queue.add(new Step(S_TEXT, "O Rei Monstro bloqueia o seu caminho!"));
        } else {
            queue.add(new Step(S_TEXT, foe.especie.nome + " apareceu!"));
        }
        after = AFTER_MENU;
        advance();
    }

    /** Pega o próximo passo da fila e mostra a mensagem. */
    private void advance() {
        Step s = queue.pollFirst();
        if (s == null) {
            finishMessages();
            return;
        }
        long now = SystemClock.uptimeMillis();
        String msg = "";
        switch (s.type) {
            case S_TEXT:
                msg = s.text;
                break;

            case S_PLAYER_ATTACK:
                msg = player.attackFisic(battleMonster);
                monsterFlashUntil = now + 500;
                if (!battleMonster.life()) {
                    world.derrotar(battleFoe, now);
                    if (battleFoe.especie.chefe) {
                        queue.addLast(new Step(S_TEXT, "O Rei Monstro foi derrotado!"));
                    } else {
                        queue.addLast(new Step(S_TEXT, "Voc\u00ea venceu!"));
                    }
                    queue.addLast(new Step(S_REWARD, null));
                    after = battleFoe.especie.chefe ? AFTER_END : AFTER_WORLD;
                } else {
                    queue.addLast(new Step(S_MONSTER_ATTACK, null));
                }
                break;

            case S_HEAL:
                msg = player.cura(HEAL_COST, player.poderCura());
                queue.addLast(new Step(S_MONSTER_ATTACK, null));
                break;

            case S_FLEE:
                if (battleFoe.especie.chefe) {
                    msg = "N\u00e3o h\u00e1 como fugir do Rei Monstro!";
                    after = AFTER_MENU;
                } else if (rnd.nextInt(100) < 60) {
                    msg = "Voc\u00ea fugiu!";
                    world.atordoar(battleFoe, now, 3000L);
                    invulnUntil = now + 2500L;
                    after = AFTER_WORLD;
                } else {
                    msg = "N\u00e3o conseguiu fugir!";
                    queue.addLast(new Step(S_MONSTER_ATTACK, null));
                }
                break;

            case S_REWARD:
                if (battleFoe.especie.chefe) {
                    msg = "A paz voltou ao reino. Sua miss\u00e3o est\u00e1 completa!";
                } else {
                    int ganho = battleFoe.especie.xp;
                    int subiu = player.ganharXp(ganho);
                    msg = "Ganhou " + ganho + " de XP.";
                    if (subiu > 0) {
                        msg += " Subiu para o n\u00edvel " + player.getLevel() + "! HP e MP restaurados.";
                    }
                }
                break;

            case S_MONSTER_ATTACK:
                msg = battleMonster.attackFisic(player);
                shakeUntil = now + 350;
                if (!player.life()) {
                    queue.addLast(new Step(S_TEXT, "Heroi desmaiou..."));
                    after = AFTER_RESPAWN;
                } else {
                    after = AFTER_MENU;
                }
                break;
        }
        showText(msg);
    }

    private void showText(String msg) {
        List<String> all = wrap(msg, TEXT_W);
        int n = Math.min(MAX_LINES, all.size());
        msgLines = new ArrayList<String>(all.subList(0, n));
        if (all.size() > MAX_LINES) {
            StringBuilder rest = new StringBuilder();
            for (int i = MAX_LINES; i < all.size(); i++) {
                if (i > MAX_LINES) rest.append(' ');
                rest.append(all.get(i));
            }
            queue.addFirst(new Step(S_TEXT, rest.toString()));
        }
        msgStart = SystemClock.uptimeMillis();
        state = MESSAGE;
    }

    private void finishMessages() {
        long now = SystemClock.uptimeMillis();
        switch (after) {
            case AFTER_MENU:
                state = MENU;
                menuIndex = 0;
                break;
            case AFTER_START:
                startWorld();
                break;
            case AFTER_END:
                scene = SCENE_BLACK;
                state = END;
                break;
            case AFTER_WORLD:
                scene = SCENE_WORLD;
                state = WORLD;
                heldDir = -1;
                break;
            case AFTER_RESPAWN:
                player.restaurarTudo();
                if (battleFoe != null) world.atordoar(battleFoe, now, 3000L);
                heroX = world.startX;
                heroY = world.startY;
                heroFromX = heroX;
                heroFromY = heroY;
                heroMoving = false;
                invulnUntil = now + 2500L;
                regiaoAtual = null;
                atualizarRegiao(now);
                scene = SCENE_WORLD;
                state = WORLD;
                heldDir = -1;
                break;
        }
    }

    private List<String> wrap(String msg, float maxW) {
        List<String> lines = new ArrayList<String>();
        String[] words = msg.split(" ");
        String cur = "";
        for (String w : words) {
            String test = cur.length() == 0 ? w : cur + " " + w;
            if (text.measureText(test) <= maxW) {
                cur = test;
            } else {
                if (cur.length() > 0) lines.add(cur);
                cur = w;
            }
        }
        if (cur.length() > 0) lines.add(cur);
        return lines;
    }

    private int visibleChars(long now) {
        long t = now - msgStart;
        return t < 0 ? 0 : (int) (t / 28);
    }

    private int totalChars() {
        int n = 0;
        for (String s : msgLines) n += s.length();
        return n;
    }

    private boolean isTyping(long now) {
        return visibleChars(now) < totalChars();
    }

    // ------------------------------------------------------------ mundo

    private void updateWorld(long now) {
        if (heroMoving && now - heroMoveStart >= HERO_STEP) {
            heroMoving = false;
        }
        if (!heroMoving && heldDir != -1) {
            tryMove(heldDir, now);
        }
        if (state == WORLD) {
            Inimigo tocou = world.update(now, heroX, heroY, now < invulnUntil);
            if (tocou != null) startBattle(tocou);
        }
    }

    private void tryMove(int dir, long now) {
        heroFacing = dir;
        int nx = heroX + DX[dir];
        int ny = heroY + DY[dir];
        if (world.isSolid(nx, ny)) return;

        Npc n = world.npcAt(nx, ny);
        if (n != null) {
            talk(n);
            return;
        }
        Inimigo m = world.inimigoAt(nx, ny);
        if (m != null) {
            if (now >= invulnUntil) startBattle(m);
            return;
        }
        heroFromX = heroX;
        heroFromY = heroY;
        heroX = nx;
        heroY = ny;
        heroMoving = true;
        heroMoveStart = now;
        atualizarRegiao(now);
    }

    private float heroDraw(boolean eixoX, long now) {
        int from = eixoX ? heroFromX : heroFromY;
        int to = eixoX ? heroX : heroY;
        if (!heroMoving) return to * TILE;
        float t = (now - heroMoveStart) / (float) HERO_STEP;
        if (t < 0f) t = 0f;
        if (t > 1f) t = 1f;
        return (from + (to - from) * t) * TILE;
    }

    private int dirFromPad(float lx, float ly) {
        float dx = lx - PAD_X;
        float dy = ly - PAD_Y;
        float r2 = dx * dx + dy * dy;
        if (r2 > PAD_R * PAD_R || r2 < 25f) return -1;
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx < 0 ? 2 : 3;
        }
        return dy < 0 ? 1 : 0;
    }

    // ------------------------------------------------------------ toque

    private int titleHit(float lx, float ly) {
        if (lx < TITLE_BTN_X0 || lx > TITLE_BTN_X1) return -1;
        for (int i = 0; i < TITLE_BTN.length; i++) {
            if (ly >= TITLE_BTN[i][0] && ly <= TITLE_BTN[i][1]) return i;
        }
        return -1;
    }

    private int hitMenu(float lx, float ly, int n) {
        if (lx < CMD_X - 4 || lx > CMD_X + CMD_W + 24) return -1;
        for (int i = 0; i < n; i++) {
            float top = CMD_Y + 6 + i * ROW_H;
            if (ly >= top - 1 && ly < top + ROW_H - 1) return i;
        }
        return -1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int a = e.getActionMasked();
        if (a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_CANCEL) {
            heldDir = -1;
            holdValid = false;
            return true;
        }

        float lx = (e.getX() - offX) / scale;
        float ly = (e.getY() - offY) / scale;
        long now = SystemClock.uptimeMillis();

        if (a == MotionEvent.ACTION_DOWN) {
            holdValid = (state == WORLD);
        }
        if (holdValid && state == WORLD) {
            if (a == MotionEvent.ACTION_DOWN || a == MotionEvent.ACTION_MOVE) {
                heldDir = dirFromPad(lx, ly);
            }
            return true;
        }
        if (a != MotionEvent.ACTION_DOWN) return true;

        switch (state) {
            case TITLE: {
                int b = titleHit(lx, ly);
                if (b == 0) {
                    state = ASK_SKIP;
                    menuIndex = 0;
                    scene = SCENE_BLACK;
                } else if (b == 1 || b == 2) {
                    titleNotice = "Em breve!";
                    titleNoticeUntil = now + 1500L;
                } else if (b == 3) {
                    Context ctx = getContext();
                    if (ctx instanceof Activity) ((Activity) ctx).finish();
                }
                break;
            }

            case ASK_SKIP: {
                int i = hitMenu(lx, ly, 2);
                if (i == 0) {
                    startWorld();
                } else if (i == 1) {
                    startIntro();
                }
                break;
            }

            case MESSAGE:
                if (isTyping(now)) {
                    msgStart = now - 100000L; // mostra o texto todo
                } else {
                    advance();
                }
                break;

            case MENU: {
                int i = hitMenu(lx, ly, 3);
                if (i == 0) {
                    after = AFTER_MENU;
                    queue.clear();
                    queue.add(new Step(S_PLAYER_ATTACK, null));
                    advance();
                } else if (i == 1) {
                    queue.clear();
                    after = AFTER_MENU;
                    if (player.getMana() < HEAL_COST) {
                        queue.add(new Step(S_TEXT, "Voc\u00ea n\u00e3o tem mana suficiente."));
                    } else {
                        queue.add(new Step(S_HEAL, null));
                    }
                    advance();
                } else if (i == 2) {
                    queue.clear();
                    after = AFTER_MENU;
                    queue.add(new Step(S_FLEE, null));
                    advance();
                }
                break;
            }

            case END:
                state = TITLE;
                scene = SCENE_BLACK;
                iniciarMusica();
                break;
        }
        return true;
    }

    // ------------------------------------------------------------ desenho

    @Override
    protected void onDraw(Canvas c) {
        long now = SystemClock.uptimeMillis();
        int w = getWidth();
        int h = getHeight();

        if (state == WORLD) updateWorld(now);
        atualizarMusica();

        c.drawColor(Color.BLACK);
        scale = Math.min(w / W, h / H);
        offX = (w - W * scale) / 2f;
        offY = (h - H * scale) / 2f;

        c.save();
        c.translate(offX, offY);
        c.scale(scale, scale);
        c.clipRect(0f, 0f, W, H);

        if (now < shakeUntil) {
            c.translate(rnd.nextInt(7) - 3, rnd.nextInt(7) - 3);
        }

        if (state == TITLE) {
            drawTitle(c, now);
        } else {
            if (scene == SCENE_WORLD) {
                drawWorld(c, now);
            } else if (scene == SCENE_BATTLE) {
                drawBattleBg(c);
                drawMonster(c, now);
                drawHeroWindow(c);
            }
            drawBottom(c, now);
        }

        c.restore();
        postInvalidateDelayed(33);
    }

    private Bitmap carregar(String nome) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;
        int id = getResources().getIdentifier(nome, "drawable", getContext().getPackageName());
        return BitmapFactory.decodeResource(getResources(), id, opts);
    }

    private Bitmap bitmapDe(Especie e) {
        Bitmap b = bmpEspecies.get(e);
        if (b == null) {
            b = carregar(e.spriteRes);
            bmpEspecies.put(e, b);
        }
        return b;
    }

    private Bitmap brancoDe(Especie e) {
        Bitmap b = bmpBrancos.get(e);
        if (b == null) {
            b = Sprites.branco(bitmapDe(e));
            bmpBrancos.put(e, b);
        }
        return b;
    }

    private Bitmap bitmapDe(Npc n) {
        Bitmap b = bmpNpcs.get(n);
        if (b == null) {
            b = carregar("npc_" + n.sprite);
            bmpNpcs.put(n, b);
        }
        return b;
    }

    private void drawWorld(Canvas c, long now) {
        float hpx = heroDraw(true, now);
        float hpy = heroDraw(false, now);

        int camX = (int) Math.floor(hpx + TILE / 2f - WORLD_W / 2f);
        int camY = (int) Math.floor(hpy + TILE / 2f - WORLD_H / 2f);
        camX = Math.max(0, Math.min(World.W * TILE - WORLD_W, camX));
        camY = Math.max(0, Math.min(World.H * TILE - WORLD_H, camY));

        c.save();
        c.scale(0.5f, 0.5f); // o mundo usa o dobro de pixels da tela virtual (tiles de 32x32)

        int tx0 = Math.max(0, camX / TILE);
        int ty0 = Math.max(0, camY / TILE);
        int tx1 = Math.min(World.W - 1, (camX + WORLD_W) / TILE);
        int ty1 = Math.min(World.H - 1, (camY + WORLD_H) / TILE);

        int brilho = (int) ((now / 450L) % 3L); // água e lava "brilham" trocando de quadro
        for (int ty = ty0; ty <= ty1; ty++) {
            for (int tx = tx0; tx <= tx1; tx++) {
                int id = world.tiles[ty][tx];
                int v = world.variante(tx, ty);
                if (id == World.AGUA || id == World.LAVA) v = (v + brilho) % 3;
                c.drawBitmap(tiles[id][v], tx * TILE - camX, ty * TILE - camY, null);
            }
        }

        for (Npc n : world.npcs) {
            if (n.x < tx0 - 1 || n.x > tx1 + 1 || n.y < ty0 - 1 || n.y > ty1 + 1) continue;
            Bitmap nb = bitmapDe(n);
            c.drawBitmap(nb, n.x * TILE - camX - (nb.getWidth() - TILE) / 2f,
                    n.y * TILE - camY - (nb.getHeight() - TILE), null);
        }

        for (Inimigo m : world.inimigos) {
            float px = m.px(now);
            float py = m.py(now);
            if (px < camX - TILE || px > camX + WORLD_W || py < camY - TILE || py > camY + WORLD_H) continue;
            int bob = (((now / 350L) + m.id) % 2L == 0L) ? 0 : -2;
            Bitmap mb = bitmapDe(m.especie);
            c.drawBitmap(mb, px - camX - (mb.getWidth() - TILE) / 2f,
                    py - camY - (mb.getHeight() - TILE) + bob, null);
        }

        // herói (pisca enquanto está invulnerável depois de fugir)
        boolean piscando = now < invulnUntil && ((now / 100L) % 2L == 0L);
        if (!piscando) {
            int passo = 0;
            if (heroMoving) passo = (((now - heroMoveStart) / 105L) % 2L == 0L) ? -2 : 0;
            c.drawBitmap(heroFrames[FRAME_DIR[heroFacing]],
                    hpx - camX + TILE / 2f - HERO_CENTER_X,
                    hpy - camY + TILE - HERO_FEET + passo, null);
        }

        c.restore();

        drawHud(c);
        drawPad(c);
        drawBanner(c, now);
    }

    private void drawHud(Canvas c) {
        window(c, 4f, 4f, 126f, 28f);
        text.setTextSize(9f);
        c.drawText("Nv " + player.getLevel() + "  XP " + player.getXp() + "/" + player.xpParaProximo(),
                12f, 15f, text);
        c.drawText("HP " + player.getHp() + "/" + player.getMaxHp()
                + " MP " + player.getMana() + "/" + player.getMaxMana(), 12f, 26f, text);
        text.setTextSize(11f);
    }

    private void drawBanner(Canvas c, long now) {
        if (bannerText == null || now >= bannerUntil) return;
        text.setTextSize(10f);
        float tw = text.measureText(bannerText);
        float bw = tw + 24f;
        float bx = (W - bw) / 2f;
        window(c, bx, 36f, bw, 20f);
        c.drawText(bannerText, bx + 12f, 50f, text);
        text.setTextSize(11f);
    }

    private void drawPad(Canvas c) {
        float[][] botoes = {
                {PAD_X, PAD_Y + 22f},   // baixo
                {PAD_X, PAD_Y - 22f},   // cima
                {PAD_X - 22f, PAD_Y},   // esquerda
                {PAD_X + 22f, PAD_Y}    // direita
        };
        for (int d = 0; d < 4; d++) {
            float cx = botoes[d][0];
            float cy = botoes[d][1];
            boolean ativo = (heldDir == d);
            fill.setColor(ativo ? 0xAAFFFFFF : 0x66000000);
            c.drawRect(cx - 11f, cy - 11f, cx + 11f, cy + 11f, fill);
            fill.setColor(0xCCFFFFFF);
            Path p = new Path();
            if (d == 0) {
                p.moveTo(cx - 5f, cy - 3f);
                p.lineTo(cx + 5f, cy - 3f);
                p.lineTo(cx, cy + 4f);
            } else if (d == 1) {
                p.moveTo(cx - 5f, cy + 3f);
                p.lineTo(cx + 5f, cy + 3f);
                p.lineTo(cx, cy - 4f);
            } else if (d == 2) {
                p.moveTo(cx + 3f, cy - 5f);
                p.lineTo(cx + 3f, cy + 5f);
                p.lineTo(cx - 4f, cy);
            } else {
                p.moveTo(cx - 3f, cy - 5f);
                p.lineTo(cx - 3f, cy + 5f);
                p.lineTo(cx + 4f, cy);
            }
            p.close();
            c.drawPath(p, fill);
        }
    }

    private void drawTitle(Canvas c, long now) {
        c.drawBitmap(titleBg, 0f, 0f, null);

        // vento levantando a capa para a esquerda
        long t = now % CAPE_CYCLE;
        int f = 0;
        if (t < CAPE_GUST) {
            f = (int) (t * CAPE_FRAMES / CAPE_GUST);
            if (f >= CAPE_FRAMES) f = CAPE_FRAMES - 1;
        }
        c.drawBitmap(capeFrames[f], CAPE_X, CAPE_Y, null);

        if (titleNotice != null && now < titleNoticeUntil) {
            text.setTextSize(8f);
            float tw = text.measureText(titleNotice);
            float bw = tw + 16f;
            float bx = (W - bw) / 2f;
            window(c, bx, 166f, bw, 13f);
            c.drawText(titleNotice, bx + 8f, 175f, text);
            text.setTextSize(11f);
        }
    }

    private void drawBottom(Canvas c, long now) {
        if (state == WORLD) return;

        if (state == ASK_SKIP) {
            drawMessageStatic(c, "Pular a introdu\u00e7\u00e3o?");
            drawMenu(c, new String[]{"Sim", "N\u00e3o"});
        } else if (state == MENU) {
            drawMessageStatic(c, "O que o Heroi vai fazer? (Curar custa " + HEAL_COST + " MP)");
            drawMenu(c, new String[]{"Atacar", "Curar", "Fugir"});
        } else if (state == END) {
            List<String> lines = new ArrayList<String>();
            lines.add("=====[   FIM   ]=====");
            lines.add("");
            lines.add("Toque na tela para jogar de novo");
            window(c, MSG_X, MSG_Y, MSG_W, MSG_H);
            for (int i = 0; i < lines.size(); i++) {
                c.drawText(lines.get(i), TEXT_X, MSG_Y + 17 + 12 * i, text);
            }
        } else {
            // MESSAGE: texto digitando
            window(c, MSG_X, MSG_Y, MSG_W, MSG_H);
            int remaining = visibleChars(now);
            boolean typing = remaining < totalChars();
            for (int i = 0; i < msgLines.size(); i++) {
                if (remaining <= 0) break;
                String ln = msgLines.get(i);
                String part = remaining >= ln.length() ? ln : ln.substring(0, remaining);
                c.drawText(part, TEXT_X, MSG_Y + 17 + 12 * i, text);
                remaining -= ln.length();
            }
            if (!typing && (now / 400) % 2 == 0) {
                triangleDown(c, MSG_X + MSG_W - 18f, MSG_Y + MSG_H - 14f);
            }
        }
    }

    private void drawMessageStatic(Canvas c, String msg) {
        window(c, MSG_X, MSG_Y, MSG_W, MSG_H);
        List<String> lines = wrap(msg, TEXT_W);
        for (int i = 0; i < lines.size() && i < MAX_LINES; i++) {
            c.drawText(lines.get(i), TEXT_X, MSG_Y + 17 + 12 * i, text);
        }
    }

    private void drawMenu(Canvas c, String[] opts) {
        float h = 12f + opts.length * ROW_H;
        window(c, CMD_X, CMD_Y, CMD_W, h);
        for (int i = 0; i < opts.length; i++) {
            float top = CMD_Y + 6 + i * ROW_H;
            if (i == menuIndex) triangleRight(c, CMD_X + 8f, top + 3f);
            c.drawText(opts[i], CMD_X + 22f, top + 11f, text);
        }
    }

    private static int lerpColor(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int g = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bl = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    /** Céu e chão coloridos atrás do monstro, de acordo com o lugar onde ele vive. */
    private void drawBattleBg(Canvas c) {
        if (battleMonster == null) return;
        Especie e = battleMonster.getEspecie();
        int topo, base, chao, chaoSombra;
        if (e == Especie.SLIME) {
            topo = 0xFF4FA8F0; base = 0xFFBFE6FF; chao = 0xFF5DC24B; chaoSombra = 0xFF3F9A33;
        } else if (e == Especie.GOBLIN) {
            topo = 0xFFB8603A; base = 0xFFF0B070; chao = 0xFF8B7355; chaoSombra = 0xFF6E5A42;
        } else if (e == Especie.SOLDADO) {
            topo = 0xFF2A1238; base = 0xFF8A2A3A; chao = 0xFF2A2030; chaoSombra = 0xFF1B1224;
        } else {
            topo = 0xFF12060C; base = 0xFF5A1018; chao = 0xFF1A0A10; chaoSombra = 0xFF0C0408;
        }
        int faixas = 10;
        float alt = 100f / faixas;
        for (int i = 0; i < faixas; i++) {
            fill.setColor(lerpColor(topo, base, i / (float) (faixas - 1)));
            c.drawRect(0f, i * alt, W, (i + 1) * alt + 1f, fill);
        }
        fill.setColor(chao);
        c.drawRect(0f, 100f, W, H, fill);
        fill.setColor(chaoSombra);
        c.drawRect(0f, 100f, W, 105f, fill);
    }

    private void drawHeroWindow(Canvas c) {
        window(c, 8f, 8f, 96f, 44f);
        text.setTextSize(10f);
        c.drawText(player.getNome() + "  Nv " + player.getLevel(), 18f, 22f, text);
        c.drawText("HP " + player.getHp() + "/" + player.getMaxHp(), 18f, 33f, text);
        c.drawText("MP " + player.getMana() + "/" + player.getMaxMana(), 18f, 44f, text);
        text.setTextSize(11f);
    }

    private void drawMonster(Canvas c, long now) {
        if (battleMonster == null) return;
        Especie esp = battleMonster.getEspecie();
        boolean alive = battleMonster.life();
        boolean flashing = now < monsterFlashUntil;
        if (!alive && !flashing) return;

        boolean white = flashing && ((now / 70) % 2 == 0);
        int bob = alive ? (int) Math.round(Math.sin(now / 350.0) * 2.0) : 0;

        Bitmap bmp = white ? brancoDe(esp) : bitmapDe(esp);
        int k = Math.max(1, 96 / Math.max(bmp.getWidth(), bmp.getHeight())); // aumento inteiro
        int w = bmp.getWidth() * k;
        int h = bmp.getHeight() * k;
        int x0 = (int) ((W - w) / 2f);
        int y0 = 102 - h + bob;
        c.drawBitmap(bmp, null, new Rect(x0, y0, x0 + w, y0 + h), null);

        if (alive) {
            fill.setColor(0xFF444444);
            c.drawRect(112f, 107f, 208f, 110f, fill);
            fill.setColor(0xFFC8321E);
            float frac = (float) battleMonster.getHp() / (float) battleMonster.getMaxHp();
            c.drawRect(112f, 107f, 112f + 96f * frac, 110f, fill);
        }
    }

    private void window(Canvas c, float x, float y, float w, float h) {
        fill.setColor(Color.WHITE);
        c.drawRect(x, y, x + w, y + h, fill);
        fill.setColor(Color.BLACK);
        c.drawRect(x + 2f, y + 2f, x + w - 2f, y + h - 2f, fill);
    }

    private void triangleRight(Canvas c, float x, float y) {
        Path p = new Path();
        p.moveTo(x, y);
        p.lineTo(x + 6f, y + 4f);
        p.lineTo(x, y + 8f);
        p.close();
        fill.setColor(Color.WHITE);
        c.drawPath(p, fill);
    }

    private void triangleDown(Canvas c, float x, float y) {
        Path p = new Path();
        p.moveTo(x, y);
        p.lineTo(x + 8f, y);
        p.lineTo(x + 4f, y + 6f);
        p.close();
        fill.setColor(Color.WHITE);
        c.drawPath(p, fill);
    }
}
