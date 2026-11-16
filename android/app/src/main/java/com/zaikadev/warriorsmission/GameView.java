package com.zaikadev.warriorsmission;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Tela do jogo em pixel art, estilo "Dragon Quest".
 * Desenha numa tela virtual de 320x180 e escala para o celular.
 */
public class GameView extends View {

    private static final float W = 320f;
    private static final float H = 180f;

    // estados da tela
    private static final int TITLE = 0, ASK_SKIP = 1, MESSAGE = 2, MENU = 3, END = 4;
    // passos da fila de mensagens
    private static final int S_TEXT = 0, S_PLAYER_ATTACK = 1, S_MONSTER_ATTACK = 2, S_HEAL = 3;
    // o que fazer quando as mensagens acabarem
    private static final int AFTER_MENU = 0, AFTER_START = 1, AFTER_END = 2;

    private static final int HEAL_COST = 100;
    private static final int HEAL_AMOUNT = 200;
    private static final int MAX_LINES = 4;
    private static final float TEXT_X = 18f;
    private static final float TEXT_W = 284f;

    // janelas (coordenadas da tela virtual)
    private static final float MSG_X = 8f, MSG_Y = 112f, MSG_W = 304f, MSG_H = 60f;
    private static final float CMD_X = 8f, CMD_Y = 58f, CMD_W = 96f;
    private static final float ROW_H = 18f;

    // sprite 16x16 do Rei Monstro
    private static final String[] SPRITE = {
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
    };

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
    private int after = AFTER_MENU;
    private int menuIndex = 0;
    private boolean inBattle = false;

    private Player player;
    private Monster monster;

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

        resetGame();
    }

    // ------------------------------------------------------------ lógica

    private void resetGame() {
        player = new Player("Heroi");
        monster = new Monster("Rei Monstro", 1000, 60);
        queue.clear();
        msgLines = new ArrayList<String>();
        monsterFlashUntil = 0;
        shakeUntil = 0;
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
        inBattle = false;
        advance();
    }

    private void startBattle() {
        resetGame();
        inBattle = true;
        queue.add(new Step(S_TEXT, "Rei Monstro apareceu!"));
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
                msg = player.attackFisic(monster);
                monsterFlashUntil = now + 500;
                if (!monster.life()) {
                    queue.addLast(new Step(S_TEXT, "Voc\u00ea venceu!"));
                    after = AFTER_END;
                } else {
                    queue.addLast(new Step(S_MONSTER_ATTACK, null));
                }
                break;
            case S_HEAL:
                msg = player.cura(HEAL_COST, HEAL_AMOUNT);
                queue.addLast(new Step(S_MONSTER_ATTACK, null));
                break;
            case S_MONSTER_ATTACK:
                msg = monster.attackFisic(player);
                shakeUntil = now + 350;
                if (!player.life()) {
                    queue.addLast(new Step(S_TEXT, "Heroi morreu..."));
                    after = AFTER_END;
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
        switch (after) {
            case AFTER_MENU:
                state = MENU;
                menuIndex = 0;
                break;
            case AFTER_START:
                startBattle();
                break;
            case AFTER_END:
                state = END;
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

    // ------------------------------------------------------------ toque

    private int hitMenu(float lx, float ly, int n) {
        if (lx < CMD_X - 4 || lx > CMD_X + CMD_W + 24) return -1;
        for (int i = 0; i < n; i++) {
            float top = CMD_Y + 6 + i * ROW_H;
            if (ly >= top - 2 && ly < top + ROW_H - 2) return i;
        }
        return -1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() != MotionEvent.ACTION_DOWN) return true;

        float lx = (e.getX() - offX) / scale;
        float ly = (e.getY() - offY) / scale;
        long now = SystemClock.uptimeMillis();

        switch (state) {
            case TITLE:
                state = ASK_SKIP;
                menuIndex = 0;
                inBattle = false;
                break;

            case ASK_SKIP: {
                int i = hitMenu(lx, ly, 2);
                if (i == 0) {
                    startBattle();
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
                int i = hitMenu(lx, ly, 2);
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
                }
                break;
            }

            case END:
                state = TITLE;
                inBattle = false;
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
            if (inBattle) {
                drawMonster(c, now);
                drawHeroWindow(c);
            }
            drawBottom(c, now);
        }

        c.restore();
        postInvalidateDelayed(33);
    }

    private void drawTitle(Canvas c, long now) {
        title.setColor(0xFFFFD21E);
        title.setTextSize(22f);
        c.drawText("WARRIOR'S MISSION", W / 2f, 78f, title);
        title.setColor(Color.WHITE);
        title.setTextSize(12f);
        c.drawText("- RPG -", W / 2f, 98f, title);
        if ((now / 500) % 2 == 0) {
            title.setTextSize(11f);
            c.drawText("Toque na tela para come\u00e7ar", W / 2f, 140f, title);
        }
    }

    private void drawBottom(Canvas c, long now) {
        if (state == ASK_SKIP) {
            drawMessageStatic(c, "Pular a introdu\u00e7\u00e3o?");
            drawMenu(c, new String[]{"Sim", "N\u00e3o"});
        } else if (state == MENU) {
            drawMessageStatic(c, "O que o Heroi vai fazer? (Curar custa " + HEAL_COST + " MP)");
            drawMenu(c, new String[]{"Atacar", "Curar"});
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
            if (i == menuIndex) triangleRight(c, CMD_X + 8f, top + 4f);
            c.drawText(opts[i], CMD_X + 22f, top + 12f, text);
        }
    }

    private void drawHeroWindow(Canvas c) {
        window(c, 8f, 8f, 96f, 44f);
        text.setTextSize(10f);
        c.drawText(player.getNome(), 18f, 22f, text);
        c.drawText("HP " + player.getHp() + "/" + player.getMaxHp(), 18f, 33f, text);
        c.drawText("MP " + player.getMana() + "/" + player.getMaxMana(), 18f, 44f, text);
        text.setTextSize(11f);
    }

    private void drawMonster(Canvas c, long now) {
        boolean alive = monster.life();
        boolean flashing = now < monsterFlashUntil;
        if (!alive && !flashing) return;

        boolean white = flashing && ((now / 70) % 2 == 0);
        int bob = alive ? (int) Math.round(Math.sin(now / 350.0) * 2.0) : 0;
        float x0 = 112f;
        float y0 = 6f + bob;
        float cell = 6f;

        for (int r = 0; r < SPRITE.length; r++) {
            String row = SPRITE[r];
            for (int col = 0; col < row.length(); col++) {
                char ch = row.charAt(col);
                if (ch == '.') continue;
                fill.setColor(white ? Color.WHITE : spriteColor(ch));
                c.drawRect(x0 + col * cell, y0 + r * cell,
                        x0 + (col + 1) * cell, y0 + (r + 1) * cell, fill);
            }
        }

        if (alive) {
            fill.setColor(0xFF444444);
            c.drawRect(112f, 107f, 208f, 110f, fill);
            fill.setColor(0xFFC8321E);
            float frac = (float) monster.getHp() / (float) monster.getMaxHp();
            c.drawRect(112f, 107f, 112f + 96f * frac, 110f, fill);
        }
    }

    private int spriteColor(char ch) {
        switch (ch) {
            case 'r': return 0xFFC8321E;
            case 'd': return 0xFF7A1010;
            case 'w': return 0xFFF2F2F2;
            case 'y': return 0xFFFFD21E;
            case 'k': return 0xFF140A0A;
            default:  return Color.MAGENTA;
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
