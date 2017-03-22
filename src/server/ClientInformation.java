/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aashi
 */
public class ClientInformation {
    
    private String username;
    private InetAddress clientIP;
    private int clientPort;
    private Socket communicationSocket;
    private DataInputStream dis;
    private DataOutputStream dos;
   

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public InetAddress getClientIP() {
        return clientIP;
    }

    public void setClientIP(InetAddress clientIP) {
        this.clientIP = clientIP;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public Socket getCommunicationSocket() {
        return communicationSocket;
    }

    public void setCommunicationSocket(Socket communicationSocket) {
        this.communicationSocket = communicationSocket;
    }

    public DataInputStream getDis() {
        return dis;
    }

    public void setDis(DataInputStream dis) {
        this.dis = dis;
    }

    public DataOutputStream getDos() {
        return dos;
    }

    public void setDos(DataOutputStream dos) {
        this.dos = dos;
    }

    public void stopCommunnication(){
        try {
            dis.close();
            dos.close();
            communicationSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientInformation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
