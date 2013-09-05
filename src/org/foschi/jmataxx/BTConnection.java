/*
 * BTConnection.java
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

//#if hasBluetooth
import javax.bluetooth.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

/**
 *
 * @author Administrator
 */
public class BTConnection implements Runnable {
    private static String jmax_UUID = "815e4c5c0bcc4346876b3a14f9185ea1";
    public static StreamConnectionNotifier notifier = null;
    private LocalDevice loc = null;
    private int oldDiscoverableStatus = 0; // to restore at exit
    private boolean master = true;
    private String playerName = null;
    private boolean end = false;
    private Vector found_devices = null;
    
    /** Creates a new instance of BTConnection */
    public BTConnection(boolean _master) {
        master = _master;
        try {
            loc = LocalDevice.getLocalDevice();
            playerName = loc.getFriendlyName();
            //
            oldDiscoverableStatus = loc.getDiscoverable();
            if (master) loc.setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);
            else loc.setDiscoverable(DiscoveryAgent.LIAC);
        } catch (BluetoothStateException ex) {
            ex.printStackTrace();
        }
    }
    
    public void run() {
        // Create connection
        if (!master) btWaitFor();
        
        // BT game loop
        end = false;
        while (!end) {
            //
        }
        
        // Reset previous discoverable status
        try {
            loc.setDiscoverable(oldDiscoverableStatus);
        } catch (BluetoothStateException ex) {
            ex.printStackTrace();
        }
    }
    
    public void stop() {
        end = true;
    }

    public Vector scanDevices() {
        found_devices = null;
        try {
            btFindDevices();
        } catch (BluetoothStateException ex) {
            ex.printStackTrace();
        }
        return found_devices;
    }
    
    // Connect to other device as a master
    private void btFindDevices() throws BluetoothStateException {
        loc.setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);
        DiscoveryAgent agent = loc.getDiscoveryAgent();
        
        // Create a listener
        InquiryListener inq = new InquiryListener();
        synchronized (inq) {
            agent.startInquiry(DiscoveryAgent.LIAC, inq);
            try {
                inq.wait();
            } catch (InterruptedException ex) {}
        }
        Enumeration inq_devices = inq.getTrovati();
        inq = null;
        
        // Get list of devices with service
        UUID[] u = new UUID[1];
        u[0] = new UUID(jmax_UUID, false);
        int attr[] = { 0x0100 };
        ServiceListener serv = new ServiceListener();
        while (inq_devices.hasMoreElements()) {
            synchronized (serv) {
                agent.searchServices(attr, u, (RemoteDevice)inq_devices.nextElement(), serv);
                try {
                    serv.wait();
                } catch (InterruptedException ex) {}
            }
        }
        Enumeration serv_devices = serv.getTrovati();
        found_devices = new Vector();
        while (serv_devices.hasMoreElements()) {
            ServiceRecord sr = (ServiceRecord)serv_devices.nextElement();
            found_devices.addElement(sr);
        }
        serv = null;
    }
    
    public void btConnect(int n) {
        ServiceRecord sr = (ServiceRecord)found_devices.elementAt(n);
        String name = (String)sr.getAttributeValue(0x0100).getValue();
        String url = sr.getConnectionURL(0, false);
        try {
            StreamConnection conn = (StreamConnection) Connector.open(url);
            InputStream is = conn.openInputStream();
            OutputStream os = conn.openOutputStream();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void btWaitFor() {
        // Wait for connection
        String url = "btspp://localhost:" +jmax_UUID+ ";name=" +playerName;
        try {
            notifier = (StreamConnectionNotifier) Connector.open(url);
            StreamConnection conn = (StreamConnection) notifier.acceptAndOpen();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // Stop waiting in other thread:
        // if (notifier != null) notifier.close();
    }
    
    private class InquiryListener implements DiscoveryListener {
        private Vector devices = null;
        public InquiryListener() {
            devices = new Vector();
        }
        
        protected Enumeration getTrovati() {
            return devices.elements();
        }
        
        public void deviceDiscovered(RemoteDevice remote, DeviceClass cod) {
            boolean ok = false;
            int major = cod.getMajorDeviceClass();
            // Accepted devices:
            if (major == 0x0100) ok = true; // Computer
            if (major == 0x0200) ok = true; // Phone
            // add device to list if accepted
            if (devices.contains(remote)) return;
            if (ok) devices.addElement(remote);
        }
        
        public void inquiryCompleted(int i) {
            synchronized (this) {
                this.notify();
            }
        }
        
        public void servicesDiscovered(int i, ServiceRecord[] serviceRecord) {
            // not implemented
        }
        
        public void serviceSearchCompleted(int a, int b) {
            // not implemented
        }
    }
    
    private class ServiceListener implements DiscoveryListener {
        private Vector devices = null;
        public ServiceListener() {
            devices = new Vector();
        }
        
        protected Enumeration getTrovati() {
            return devices.elements();
        }
        
        public void servicesDiscovered(int i, ServiceRecord[] serviceRecord) {
            devices.addElement(serviceRecord[0]);
        }
        
        public void serviceSearchCompleted(int a, int b) {
            synchronized (this) {
                this.notify();
            }
        }
        
        public void deviceDiscovered(RemoteDevice remote, DeviceClass cod) {
            // not implemented
        }
        
        public void inquiryCompleted(int i) {
            // not implemented
        }
    }
//#else
//# public class BTConnection {
//#endif
}
