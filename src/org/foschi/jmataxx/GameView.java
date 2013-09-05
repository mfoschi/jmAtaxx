/*
 * GameView.java
 *
 ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is Marcello Foschi.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 ***** END LICENSE BLOCK *****
 *
 */

package org.foschi.jmataxx;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.midlet.MIDlet;

/**
 *
 * @author Marcello Foschi
 */
public class GameView extends GameCanvas {
    private MIDlet app = null;
    private boolean multi = false;
    private GameBoard board = null;
    private AbstractManager man = null;
    private boolean firstTime = true;
    private int width,height;
    private int board_x, board_y, board_size;
    private int mess_x, mess_y, status_y, status_size;
    private int step;
    // Cursore
    private boolean cursor_on = false;
    private Move move = null;
    private static final String[] names = {"Dark","Light","Game Over!"};
    // 0: eventi player, 1: attende player AI o BT, 2: game over
    private int state = 0;
    String stat_mess = null;
    String score_mess = null;
    private boolean repeat = false;
    
    /**
     * Creates a new instance of GameView
     */
    public GameView(MIDlet _app, boolean _multi) {
        super(false);
        setFullScreenMode(true);
        app = _app;
        multi = _multi;
        board = new GameBoard();
        stat_mess = "Starting game...";
        score_mess = "Dark: 0 - Light: 0";
        int level = Prefs.getInstance().getLevel();
        if (multi) man = new BTManager();
        else man = new AIManager(board);
        move = new Move(new Cell(3,3), new Cell(3,3));
        getKeyStates(); // reset key pressed before starting
        stat_mess = "Now playing: " + names[state];
    }
    
    // Gestione eventi
    
    protected void keyPressed(int keyCode) {
        if (state != 0) return;
        int action = getGameAction(keyCode);
        if (action == Canvas.FIRE) execAction();
        Cell cursor = null;
        if (cursor_on) cursor = move.getTarget();
        else cursor = move.getStart();
        switch (action) {
            case Canvas.UP:
                cursor.moveUp();
                break;
            case Canvas.DOWN:
                cursor.moveDown();
                break;
            case Canvas.LEFT:
                cursor.moveLeft();
                break;
            case Canvas.RIGHT:
                cursor.moveRight();
                break;
        }
        repaint();
    }
    
    protected void pointerPressed(int _x, int _y) {
        if (state != 0) return;
        // trova la cella in cui si trovano le coordinate e sposta il cursore
        int targ_c = (_x - board_x)/step;
        int targ_r = (_y - board_y)/step;
        Cell target = new Cell(targ_r,targ_c);
        move.setTarget(target);
        int side = board.getFrom(target);
        if (side == -1) execAction();
        else if (side == 0) {
            move.setStart(target);
            cursor_on = true;
        }
        repaint();
    }
    
    private void execAction() {
        if (!cursor_on) {
            Cell start = move.getStart();
            move.setTarget(start);
            if (board.cellOwned(start)) cursor_on = true;
            return;
        }
        endTurn(0);
        repaint();
        while (state == 1) {
            move = man.doTurn();
            endTurn(1);
            repaint();
        }
    }
    
    private void endTurn(int turn) {
        if (!board.movePiece(turn,move)) return;
        //
        state = 1 - turn;
        stat_mess = "Now playing: " + names[state];
        move.setStart(new Cell(3,3));
        cursor_on = false;
        // Verifica le condizioni di fine partita
        boolean ok[] = board.hasMoves();
        if ((!ok[state]) && repeat) state = 2;
        //
        int[] count = board.getCount();
        if ((count[0]==0) || (count[1]==0)) state = 2;
        score_mess = "Dark: " +count[0]+ " - Light: " +count[1];
        //
        if ((!ok[0])&&(!ok[1])) state = 2;
        //
        if (state == 2) {
            stat_mess = "GAME OVER!";
            Prefs p = Prefs.getInstance();
            p.addPlayed();
            if (count[0]>count[1]) {
                p.addVictory();
                stat_mess += " You won!";
            } else stat_mess += " You lost!";
            return;
        }
        //
        if (!ok[state]) {
            repeat = true;
            stat_mess = "No moves for player " + names[1];
            stat_mess += " - again: " + names[0];
            state = turn;
            return;
        } else repeat = false;
    }
    
    
    // Gestione grafica
    protected void sizeChanged(int w, int h) {
        width = w;
        height = h;
        firstTime = true;
        repaint();
    }
    
    public void paint(Graphics g) {
        if (firstTime) {
            // x and y are the coordinates of the top corner of the display area:
            int x = g.getClipX();
            int y = g.getClipY();
            // w and h are the width and height of the display area:
            width = g.getClipWidth();
            height = g.getClipHeight();
            // calcola l'altezza delle aree di status/message
            status_size = 2 + g.getFont().getHeight();
            mess_x = x;
            mess_y = y;
            status_y = height - status_size - mess_y;
            // calcola la finestra di gioco
            board_size = height - 2*status_size;
            if (board_size > width) {
                board_size = width;
            }
            board_y = status_y - board_size;
            board_x = (width - board_size)/2 + x;
            step = (board_size-1)/7;
            board_size = (7*step)+1; // arrotonda a numero intero
            // only updates coordinates first time (fixed size)
            firstTime = false;
        }
        // pulisce il display
        g.setColor(255, 255, 255);
        g.fillRect(0, 0, getWidth(), getHeight());
        // disegna la scacchiera
        updateView(g);
        // aggiorna lo stato
        g.setColor(0, 0, 0);
        int[] count = board.getCount();
        score_mess = "Dark: " +count[0]+ " - Light: " +count[1];
        g.drawString(score_mess, width/2, mess_y+2, Graphics.TOP|Graphics.HCENTER);
        g.drawString(stat_mess, width/2, status_y+2, Graphics.TOP|Graphics.HCENTER);
        // disegna il cursore
        drawCursor(g);
    }
    
    public void updateView(Graphics g) {
        // disegna un rettangolo rosso che fa da contorno al display
        g.setColor(80, 0, 0);
        g.drawRect(mess_x, mess_y, width-1, status_size-1);
        g.drawRect(board_x, board_y, board_size-1, board_size-1);
        g.drawRect(mess_x, status_y, width-1, status_size-1);
        // disegna una griglia verde su sfondo sabbia
        g.setColor(208, 208, 128);
        g.fillRect(board_x+1, board_y+1, board_size-2, board_size-2);
        g.setColor(0, 176, 0);
        for (int i=1; i<7; i++) {
            g.drawLine(board_x, board_y+i*step, board_x+7*step, board_y+i*step);
            g.drawLine(board_x+i*step, board_y, board_x+i*step, board_y+7*step);
        }
        // disegna i pezzi
        board.drawPieces(g, board_x, board_y, step);
    }
    
    private void drawCursor(Graphics g) {
        int _x,_y;
        Cell start = move.getStart();
        int cur_r = start.getRow();
        int cur_c = start.getCol();
        g.setColor(128, 128, 128);
        if (cursor_on) {
            // Disegna le mosse possibili
            for (int i=cur_r-2; i<=cur_r+2; i++) {
                if ((i<0)||(i>6)) continue;
                for (int j=cur_c-2; j<=cur_c+2; j++) {
                    if ((j<0)||(j>6)) continue;
                    if (board.cellBusy(i,j)) continue;
                    _y = board_y + i*step + 2;
                    _x = board_x + j*step + 2;
                    g.drawRoundRect(_x, _y, step-4, step-4, 2, 2);
                }
            }
            // Disegna il cursore di target
            Cell target = move.getTarget();
            _x = board_x + step*target.getCol();
            _y = board_y + step*target.getRow();
            g.setColor(255, 0, 255); // target: pink
            g.drawLine(_x, _y+step/2, _x+step, _y+step/2);
            g.drawLine(_x+step/2, _y, _x+step/2, _y+step);
            // g.drawRoundRect(_x, _y, step-4, step-4, 2, 2);
            if (state == 0) g.setColor(208, 208, 208); // cursor on dark: light gray
            else g.setColor(0, 0, 0); // cursor on light: black
        }
        // Disegna il cursore di origine
        _x = board_x + cur_c*step + 3;
        _y = board_y + cur_r*step + 3;
        g.drawRoundRect(_x, _y, step-5, step-5, 3, 3);
    }

}
