/*
 * jmAtaxxMidlet.java
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

import java.io.IOException;
import java.util.Vector;
import javax.microedition.io.StreamConnectionNotifier;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 *
 * @author Marcello Foschi
 */
public class jmAtaxxMidlet extends MIDlet implements CommandListener {
    private Display disp = null;
    private BTConnection btLoop = null;
    // screens
    private List mainMenu = null;
    private List multiMenu = null;
    private List devList = null;
    private GameView boardView = null;
    // commands
    private Command exitCommand = null;
    private Command backCommand = null;
    private Command doneCommand = null;
    private Command cancelCommand = null;
    private Command selectCommand = null;
    private Command multiCommand = null;
    private Command connCommand = null;
    private Command rescanCommand = null;
    //
    private TextField txtName = new TextField("Player name:", "", 20, TextField.ANY);
    private ChoiceGroup cg = new ChoiceGroup("Type of opponent", Choice.EXCLUSIVE);
    private String[] levels = {
        "0: BT Player",
        "1: Very Easy AI",
        "2: Easy AI",
        "3: Medium AI",
        "4: AI level 4",
        "5: AI level 5",
        "6: AI level 6"
    };


    
    public jmAtaxxMidlet() {
        exitCommand = new Command("Exit", Command.EXIT, 1);
        backCommand = new Command("Back", Command.BACK, 2);
        doneCommand = new Command("Done", Command.OK, 2);
        cancelCommand = new Command("Cancel", Command.STOP, 2);
        // Different types of "select" command:
        selectCommand = new Command("Select", Command.ITEM, 2);
        multiCommand = new Command("Select", Command.ITEM, 2);
        connCommand = new Command("Connect", Command.ITEM, 2);
        rescanCommand = new Command("Rescan", Command.ITEM, 2);
    }
    
    public void startApp() {
        disp = Display.getDisplay(this);
        displayMainMenu();
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
        disp.setCurrent(null);
        mainMenu = null;
        devList = null;
        boardView = null;
        notifyDestroyed();
    }
    
    public void commandAction(Command command, Displayable displayable) {
        if (command == selectCommand) {
            switch (mainMenu.getSelectedIndex()) {
                case 0: // Play Solo
                    displayBoardView(false);
                    break;
                case 1: // Multiplayer
//#if hasBluetooth
                    displayMultiMenu();
//#else
//#                     Alert noBT = new Alert("No BT available");
//#                     disp.setCurrent(noBT, mainMenu);
//#endif
                    break;
                case 2: // Config
                    displayConfigForm();
                    break;
                case 3: // Stats
                    displayStatsForm();
                    break;
                case 4: // Exit
                    destroyApp(true);
                    break;
            }
        }
//#if hasBluetooth
        if (command == multiCommand) {
            switch (multiMenu.getSelectedIndex()) {
                case 0: // Create a new Game
                    btLoop = new BTConnection(true);
                    Vector found = btLoop.scanDevices();
                    displayDevicesList(found);
                    break;
                case 1: // Connect to existing
                    btLoop = new BTConnection(false);
                    displayConnectingAlert();
                    btLoop.run();
                    break;
                case 2: // Back to Main
                    displayMainMenu();
                    break;
            }
        }
        if (command == rescanCommand) {
            Vector found = btLoop.scanDevices();
            displayDevicesList(found);
        }
        if (command == connCommand) {
            int n = devList.getSelectedIndex();
            btLoop.btConnect(n);
        }
        if (command == cancelCommand) {
            StreamConnectionNotifier notifier = BTConnection.notifier;
            try {
                if (notifier == null) btLoop.stop();
                else notifier.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            displayMultiMenu();
        }
//#endif
        if (command == doneCommand) {
            Prefs p = Prefs.getInstance();
            p.setLevel(cg.getSelectedIndex() +1);
            p.setPlayerName(txtName.getString());
            p.savePrefs();
            displayMainMenu();
        }
        if (command == backCommand) {
            displayMainMenu();
        }
        if (command == exitCommand) {
            destroyApp(true);
        }
    }
    
    private void displayMainMenu() {
        if (mainMenu == null) {
            mainMenu = new List("Main Form", List.IMPLICIT);
            mainMenu.append("Play Solo", null); // 0
            mainMenu.append("Multiplayer", null); // 1
            mainMenu.append("Config", null); // 2
            mainMenu.append("Stats", null); // 3
            mainMenu.append("Exit", null); // 4
            mainMenu.addCommand(exitCommand);
            mainMenu.setSelectCommand(selectCommand);
            mainMenu.setCommandListener(this);
        }
        disp.setCurrent(mainMenu);
    }
    
//#if hasBluetooth
    private void displayMultiMenu() {
        if (multiMenu == null) {
            // Check if BT available
            multiMenu = new List("Bluetooth", List.IMPLICIT);
            multiMenu.append("Start a new game", null); // 0
            multiMenu.append("Connect to game", null); // 1
            multiMenu.append("Back to main menu", null); // 2
            multiMenu.setSelectCommand(multiCommand);
            multiMenu.setCommandListener(this);
        }
        disp.setCurrent(multiMenu);
    }
    
    private void displayDevicesList(Vector found) {
        devList = new List("Devices", List.IMPLICIT);
        for (int i=0; i<found.size(); i++) {
//            javax.bluetooth.ServiceRecord sr = (ServiceRecord)found.elementAt(i);
//            String devName = (String)sr.getAttributeValue(0x0100).getValue();
            String devName = (String)found.elementAt(i);
            devList.append(devName, null);
        }
        devList.addCommand(backCommand);
        if (found.size() > 0) devList.setSelectCommand(connCommand);
        else devList.addCommand(rescanCommand);
        devList.setCommandListener(this);
        disp.setCurrent(devList);
    }
    
    private void displayConnectingAlert() {
        Form connectAlert = new Form("Waiting");
        Gauge waiting = new Gauge("Waiting for connection...",
                false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
        connectAlert.append(waiting);
        connectAlert.addCommand(cancelCommand);
        connectAlert.setCommandListener(this);
        disp.setCurrent(connectAlert);
    }
//#endif
    
    private void displayBoardView(boolean multi) {
        boardView = new GameView(this, multi);
        boardView.addCommand(backCommand);
        boardView.setCommandListener(this);
        disp.setCurrent(boardView);
        boardView.repaint();
    }
    
    private void displayConfigForm() {
        Prefs p = Prefs.getInstance();
        Form configForm = new Form("Game Config");
        txtName.setString(p.getPlayerName());
        configForm.append(txtName);
        cg.deleteAll();
        for (int i = 1; i<levels.length; i++)
            cg.append(levels[i], null);
        cg.setSelectedIndex(p.getLevel() +1, true);
        configForm.append(cg);
        configForm.addCommand(backCommand);
        configForm.addCommand(doneCommand);
        configForm.setCommandListener(this);
        disp.setCurrent(configForm);
    }
    
    private void displayStatsForm() {
        Form statsForm = new Form("Game Stats");
        Prefs p = Prefs.getInstance();
        statsForm.append(new StringItem("Player: ", p.getPlayerName()));
        statsForm.append(new StringItem("Opponent: ", levels[p.getLevel()]));
        statsForm.append(new StringItem("Games won/played:", null));
        String[] scores = p.getScores();
        for (int i = 0; i<scores.length; i++)
            statsForm.append(new StringItem(levels[i]+": ", scores[i]));
        statsForm.addCommand(backCommand);
        statsForm.setCommandListener(this);
        disp.setCurrent(statsForm);
    }
    
}
