/*
 * GameBoard.java
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

import javax.microedition.lcdui.Graphics;

/**
 *
 * @author Marcello Foschi
 */
public class GameBoard {
    private int[][] allPieces; // indici validi: 0..6
    
    /** Creates a new instance of GameBoard */
    public GameBoard() {
        allPieces = new int[7][7];
        for (int _row=0; _row<7; _row++) {
            for (int _col=0; _col<7; _col++) {
                allPieces[_row][_col] = -1;
            }
        }
        createPiece(0,0,6);
        createPiece(0,6,0);
        createPiece(1,0,0);
        createPiece(1,6,6);
    }
    
    private GameBoard(int[][] _allPieces) {
        allPieces = new int[7][7];
        for (int _row=0; _row<7; _row++) {
            for (int _col=0; _col<7; _col++) {
                allPieces[_row][_col] = _allPieces[_row][_col];
            }
        }
    }
    
    // DATA
    
    public int getHeuristic(int side) {
        int h = 0;
        int count[] = getCount();
        h = count[side] - count[1-side];
        return h;
    }
    
    public int[] getCount() {
        int count[] = {0,0};
        for (int _row=0; _row<7; _row++) {
            for (int _col=0; _col<7; _col++) {
                int p = allPieces[_row][_col];
                if (p == -1) continue;
                count[p]++;
            }
        }
        return count;
    }
    
    public boolean[] hasMoves() {
        // verifica se i giocatori hanno mosse possibili
        boolean ok[] = {false,false};
        for (int _row=0; _row<7; _row++) {
            for (int _col=0; _col<7; _col++) {
                // cerca una cella libera
                if (allPieces[_row][_col] != -1) continue;
                // cerca almeno una pedina di ogni giocatore entro distanza 2
                for (int i=Math.max(0,_row-2); i<Math.min(7,_row+3); i++)
                    for (int j=Math.max(0,_col-2); j<Math.min(7,_col+3); j++) {
                    int p = allPieces[i][j];
                    if (p == -1) continue;
                    ok[p] = true;
                    if (ok[0] && ok[1]) break;
                    }
            }
        }
        return ok;
    }
    
    public boolean createPiece(int side, int _row, int _col) {
        if (allPieces[_row][_col] != -1) return false;
        allPieces[_row][_col] = side;
        return true;
    }
    
    public boolean movePiece(int turn, Move move) {
        boolean ok = true;
        Cell start = move.getStart();
        Cell target = move.getTarget();
        int targ_r = target.getRow();
        int targ_c = target.getCol();
        int dx = move.getDeltaX();
        int dy = move.getDeltaY();
        // distanza eccessiva
        if ((dx>2)||(dy>2)) return false;
        // target == cursor (distanza 0)
        if ((dx==0)&&(dy==0)) return false;
        // cursor -> target distanza 1
        if ((dx<2)&&(dy<2)) {
            // new piece
            ok = createPiece(turn, targ_r, targ_c);
        } else {
            // jump
            int start_r = start.getRow();
            int start_c = start.getCol();
            int p = allPieces[start_r][start_c];
            if (p == -1) ok = false;
            else {
                allPieces[start.getRow()][start.getCol()] = -1;
                allPieces[targ_r][targ_c] = turn;
            }
        }
        if (ok) {
            // inverte i pezzi avversari adiacenti
            for (int i=targ_r-1; i<=targ_r+1; i++) {
                if ((i<0)||(i>6)) continue;
                for (int j=targ_c-1; j<=targ_c+1; j++) {
                    if ((j<0)||(j>6)) continue;
                    int p = allPieces[i][j];
                    if (p == -1) continue;
                    if (p != turn) allPieces[i][j]=turn;
                }
            }
        }
        return ok;
    }
    
    public boolean cellBusy(int _row, int _col) {
        return (allPieces[_row][_col] != -1);
    }
    
    public boolean cellBusy(Cell pos) {
        return (allPieces[pos.getRow()][pos.getCol()] != -1);
    }
    
    public boolean cellOwned(Cell pos) {
        int side = allPieces[pos.getRow()][pos.getCol()];
        if (side == -1) return false;
        if (side == 0) return true;
        else return false;
    }
    
    public int getFrom(Cell pos) {
        return allPieces[pos.getRow()][pos.getCol()];
    }
    
    public void drawPieces(Graphics g, int x, int y, int step) {
        int size = step-5;
        for (int _row=0; _row<7; _row++) {
            for (int _col=0; _col<7; _col++) {
                int side = allPieces[_row][_col];
                if (side == -1) continue;
                int pos_x = x + _col*step + 2;
                int pos_y = y + _row*step + 2;
                // Dark: blue, Light: sand
                if (side == 0) g.setColor(0, 0, 80);
                else g.setColor(176, 176, 80);
                g.fillArc(pos_x, pos_y, size, size, 0, 360);
                // Contorno grigio
                g.setColor(208,208,208);
                g.drawArc(pos_x, pos_y, size, size, 0, 360);
            }
        }
    }
    
    public GameBoard clone() {
        GameBoard temp = new GameBoard(allPieces);
        return temp;
    }
    
}
