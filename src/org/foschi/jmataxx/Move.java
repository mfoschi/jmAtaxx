/*
 * Move.java
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
public class Move {
    private Cell start;
    private Cell target;
    
    /** Creates a new instance of Move */
    public Move(Cell _start, Cell _target) {
        start = _start.clone();
        target = _target.clone();
    }

    public Cell getStart() {
        return start;
    }

    public void setStart(Cell _start) {
        start = _start.clone();
    }

    public Cell getTarget() {
        return target;
    }

    public void setTarget(Cell _target) {
        target = _target.clone();
    }

    int getDeltaX() {
        return Math.abs(target.getCol() - start.getCol());
    }

    int getDeltaY() {
        return Math.abs(target.getRow() - start.getRow());
    }
    
    
}