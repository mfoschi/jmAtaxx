/*
 * BTManager.java
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

import javax.microedition.io.StreamConnectionNotifier;

/**
 * Manager for remote Bluetooth slave.
 * @author Marcello Foschi
 */
public class BTManager extends AbstractManager {
    
    /** Creates a new instance of BTManager */
    public BTManager() {
    }
    
    public Move doTurn() {
        Move remote = null;
        // Waits for remote turn
        // remote = ...
        return remote;
    }
}
