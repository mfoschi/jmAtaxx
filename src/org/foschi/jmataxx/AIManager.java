/*
 * AIManager.java
 *
 * Original alphabeta algorithm idea by Sjoerd Langkemper
 * URL: http://www.linuxonly.nl/docs/ataxx
 * C/GTK implementation by Sjoerd Langkemper
 * URL: http://www.linuxonly.nl/docs/gataxx
 *
 * Java porting and adaptation by Marcello Foschi (2006)
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

import java.util.Random;
import java.util.Vector;

/**
 *
 * @author Sjoerd Langkemper
 * @author Marcello Foschi
 */
public class AIManager extends AbstractManager {
    private GameBoard board = null;
    
    public AIManager(GameBoard _board) {
        super();
        board = _board;
    }
    
    public Move doTurn() {
        int lev = Prefs.getInstance().getLevel();
        if (lev == 1) return doTurn_level1();
        if (lev == 2) return doTurn_level2();
        if (lev >= 3) return doTurn_levelN(lev-3);
        // non dovrebbero esserci altri valori
        return null;
    }
    
    private Move doTurn_level1() {
        Move result = null;
        Vector pm = getPossibleMoves(1);
        // Random move
        if (pm.size() > 0) {
            int i = new MyRandom().nextInt(pm.size());
            result = (Move)pm.elementAt(i);
        }
        return result;
    }
    
    private Move doTurn_level2() {
        Move result = null;
        Vector allMoves = getPossibleMoves(1);
        
        int maxh = -100;
        for (int i=0; i<allMoves.size(); i+=5) {
            GameBoard test = board.clone();
            Move temp = (Move)allMoves.elementAt(i);
            test.movePiece(1, temp);
            int h = test.getHeuristic(1);
            if (h > maxh) {
                maxh = h;
                result = temp;
            }
        }
        return result;
    }
    
    private Move doTurn_levelN(int depth) {
        Move result = null;
        Vector allMoves = getPossibleMoves(1);
        Vector bestMoves = new Vector();
        
        int maxh = -100;
        for (int i=0; i<allMoves.size(); i++) {
            GameBoard test = board.clone();
            Move temp = (Move)allMoves.elementAt(i);
            test.movePiece(1, temp);
            int h = alphaBeta(test, -100, 100, 0, 1, depth);
            Move m = (Move)allMoves.elementAt(i);
            if (h>maxh) {
                maxh=h;
                bestMoves.removeAllElements();
                bestMoves.addElement(m);
            } else if (h==maxh) {
                bestMoves.addElement(m);
            }
        }
        int j = new MyRandom().nextInt(bestMoves.size());
        result = (Move)bestMoves.elementAt(j);
        return result;
    }
    
    int alphaBeta(GameBoard _test, int alpha, int beta, int side, int me, int depth) {
        int ab = 0;
        if (depth==0) {
            ab = _test.getHeuristic(me);
            if (ab==0) return -50;
            if (_test.getHeuristic(1-me) == 0) return 50;
        } else {
            Vector allMoves = getPossibleMoves(side);
            for (int i=0; i<allMoves.size(); i++) {
                GameBoard test = _test.clone();
                Move temp = (Move)allMoves.elementAt(i);
                test.movePiece(1, temp);
                int h = alphaBeta(test, alpha, beta, 1-side, me, depth-1);
                if ((side==1)&&(h>alpha)) alpha=h;
                if ((side==0)&&(h<beta)) beta=h;
                if (alpha>beta) break;
            }
            if (side==1) ab = alpha;
            else ab = beta;
        }
        return ab;
    }
    
    private Vector getPossibleMoves(int side) {
        Vector moves = new Vector();
        for (int _row=0; _row<7; _row++) {
            for (int _col=0; _col<7; _col++) {
                // for eache empty box
                Cell target = new Cell(_row,_col);
                if (board.cellBusy(target)) continue;
                // find a normal move
                Move m = null;
                for (int r=Math.max(0, _row-1); r<Math.min(7, _row+2); r++) {
                    if (m != null) break;
                    for (int c=Math.max(0, _col-1); c<Math.min(7, _col+2); c++) {
                        if (m != null) break;
                        Cell start = new Cell(r,c);
                        int piece = board.getFrom(start);
                        if (piece==side) m = new Move(start, target);
                    }
                }
                if (m == null) {
                    // add all jump moves
                    for (int r=Math.max(0, _row-2); r<Math.min(7, _row+3); r++) {
                        for (int c=Math.max(0, _col-2); c<Math.min(7, _col+3); c++) {
                            Cell start = new Cell(r,c);
                            int piece = board.getFrom(start);
                            if (piece==side) moves.addElement(new Move(start, target));
                        }
                    }
                } else moves.addElement(m);
            }
        }
        return moves;
    }
    
    // For CLDC 1.0 devices
    private class MyRandom extends Random {
        public int nextInt(int n) {
            if (n<=0)
                throw new IllegalArgumentException("n must be positive");

            if ((n & -n) == n)  // i.e., n is a power of 2
                return (int)((n * (long)next(31)) >> 31);

            int bits, val;
            do {
                bits = next(31);
                val = bits % n;
            } while(bits - val + (n-1) < 0);
            return val;
        }
    }
    
}
