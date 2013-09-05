/*
 * Cell.java
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

/**
 *
 * @author Marcello Foschi
 */
public class Cell {
    private int row;
    private int col;
    
    /** Creates a new instance of Cell */
    public Cell(int _row, int _col) {
        row = _row;
        col = _col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void moveUp() {
        if (row > 0) row--;
    }

    public void moveDown() {
        if (row < 6) row++;
    }

    public void moveLeft() {
        if (col > 0) col--;
    }

    public void moveRight() {
        if (col < 6) col++;
    }

    public Cell clone() {
        return new Cell(row,col);
    }
    
}
