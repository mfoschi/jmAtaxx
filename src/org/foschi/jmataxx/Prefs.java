/*
 * Prefs.java
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreNotFoundException;

/**
 *
 * @author Marcello Foschi
 */
public class Prefs {
    private RecordStore rs = null;
    private final static int maxLevel = 5;
    private int level = 1; // 0: BT, 1: easy, 2: medium, 3+: hard (depth level-3)
    private String playerName = "Player";
    private byte[] played = null;
    private byte[] victories = null;
    
    /** Singleton instance */
    private static Prefs instance = null;
    private Prefs() {
        played = new byte[maxLevel+1];
        victories = new byte[maxLevel+1];
        loadPrefs();
    }
    public static Prefs getInstance() {
        if (instance == null) instance = new Prefs();
        return instance;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String _playerName) {
        playerName = _playerName;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int _level) {
        level = _level;
    }
    
    public void addPlayed() {
        played[level]++;
    }
    
    public void addVictory() {
        victories[level]++;
    }
    
    String[] getScores() {
        String[] scores = new String[maxLevel+1];
        for (int i=0; i<=maxLevel; i++) scores[i] = victories[i]+" / "+played[i];
        return scores;
    }
    
    // PERSISTENCE
    
    private void loadPrefs() {
        try {
            rs = RecordStore.openRecordStore("Ataxx", false);
            if (rs.getNumRecords() != 3) return;
            // livello corrente e nome giocatore
            ByteArrayInputStream bais = new ByteArrayInputStream(rs.getRecord(1));
            DataInputStream inStream = new DataInputStream(bais);
            level = inStream.readInt();
            if (level > maxLevel) level = maxLevel;
            playerName = inStream.readUTF();
            inStream.close();
            bais.close();
            // numero partite giocate/vinte
            rs.getRecord(2, played, 0);
            rs.getRecord(3, victories, 0);
        } catch (RecordStoreNotFoundException ex) {
            // If not existings simply skip reading
            rs = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.closeRecordStore();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    public void savePrefs() {
        try {
            rs = RecordStore.openRecordStore("Ataxx", true);
            // livello corrente e nome giocatore
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream outStream = new DataOutputStream(baos);
            outStream.writeInt(level);
            outStream.writeUTF(playerName);
            byte[] temp = baos.toByteArray();
            outStream.close();
            baos.close();
            if (rs.getNumRecords() == 0) {
                // CREATE new records
                rs.addRecord(temp, 0, temp.length);
                rs.addRecord(played, 0, maxLevel+1);
                rs.addRecord(victories, 0, maxLevel+1);
            } else {
                // UPDATE existing records
                rs.setRecord(1, temp, 0, temp.length);
                rs.setRecord(2, played, 0, maxLevel+1);
                rs.setRecord(3, victories, 0, maxLevel+1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.closeRecordStore();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
}
