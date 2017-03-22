/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import client.ChatClient;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import server_client_message_protocol.BroadcastMessage;
import server_client_message_protocol.ConnectMessage;
import server_client_message_protocol.DisconnectMessage;
import server_client_message_protocol.Message;
import server_client_message_protocol.PersonalMessage;
import server_client_message_protocol.UDPMessage;

/**
 *
 * @author aashi
 */
public class ServerCommunicator{
    
    private ClientInformation clientInformation;
    private boolean usernameValid;
    private boolean tcpCommunicationStarted;
    
    public ServerCommunicator(Socket tcpSocket){
        clientInformation = new ClientInformation();
        clientInformation.setCommunicationSocket(tcpSocket);
        clientInformation.setUsername(null);
        this.usernameValid = false;
        try {
            clientInformation.setDis(new DataInputStream(tcpSocket.getInputStream()));
            clientInformation.setDos(new DataOutputStream(tcpSocket.getOutputStream()));
        } catch (IOException ex) {
            Logger.getLogger(ServerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Communication link with client could not be established successfully");
            
        }
        new Thread(new TCPMessageListener()).start();
    }

    private void validateUsername(){
        try {
            System.out.println("Waiting for client input");
            clientInformation.setUsername(clientInformation.getDis().readUTF());
        } catch (IOException ex) {
            Logger.getLogger(ServerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Received input from client");
        while(!usernameValid){
            usernameValid = ChatServer.isUsernameAcceptable(clientInformation.getUsername());
            if(usernameValid){
                System.out.println("Adding username to online list");
                if(ChatServer.addClientToClientSocketMap(this)){
                    try {
                        //send an ackonwledgement to the client that the username was registered
                        clientInformation.getDos().writeUTF(Message.USERNAME_ACCEPTED);
                        System.out.println("Sent acknowledgement to client");
                    } catch (IOException ex) {
                        Logger.getLogger(ServerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            else{
                try {
                    System.out.println("Ask Client for another username");
                    clientInformation.getDos().writeUTF(Message.USERNAME_NOT_ACCEPTED);
                    //ask for another username
                    clientInformation.setUsername(clientInformation.getDis().readUTF());
                } catch (IOException ex) {
                    Logger.getLogger(ServerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        }
    }
    
    public void stopCommunication(){
        tcpCommunicationStarted = false;
        ChatServer.removeClientFromClientToSocketMap(clientInformation.getUsername());
        try {
            clientInformation.getDis().close();
            clientInformation.getDos().close();
            clientInformation.getCommunicationSocket().close();
        } catch (IOException ex) {
            Logger.getLogger(ServerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Message getDecodedMessage(int messageLength){
        try {
            
            byte[] encapsulatedClientMessage = new byte[messageLength];
            clientInformation.getDis().read(encapsulatedClientMessage);
            return Message.getDecodedMessage(encapsulatedClientMessage);
            
        } catch (IOException ex) {
            Logger.getLogger(ServerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private void processClientMessage(Message message){
        System.out.println("Processing client request");
        if(message instanceof PersonalMessage){
            sendPersonalMessage((PersonalMessage) message);
        }
        else if(message instanceof BroadcastMessage){
            sendBroadcastMessage((BroadcastMessage) message);
        }
        else if(message instanceof DisconnectMessage){ 
            ChatServer.sendDisconnectMessage((DisconnectMessage) message);
        }
        else{
            ChatServer.sendConnectMessage((ConnectMessage) message);
        }
    }
    
    private void sendPersonalMessage(PersonalMessage message){
        Socket receivingClientSocket = ((ChatServer.getConnectedClientAndSockets()).get(message.getSendTo())).getCommunicationSocket();
        sendMessageThroughSocket(receivingClientSocket, message);
    }
    
    private void sendBroadcastMessage(BroadcastMessage message){
        List<ClientInformation> clientInformationList = new ArrayList<>(ChatServer.getConnectedClientAndSockets().values());
        for(ClientInformation clientInformation : clientInformationList){
            sendMessageThroughSocket(clientInformation.getCommunicationSocket(), message);
        }
    }
    
    private void sendMessageThroughSocket(Socket socket, Message message){
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            byte[] encapsulatedMessage = message.getEncapsulatedMessage();
            dos.writeInt(encapsulatedMessage.length);
            dos.write(encapsulatedMessage);
            System.out.println("Message routed to the client");
        } catch (RemoteException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ServerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    private class TCPMessageListener implements Runnable{
        @Override
        public void run() {
            validateUsername();
            tcpCommunicationStarted = true;
            while(tcpCommunicationStarted){
                try {
                    int clientMessageLength =  clientInformation.getDis().readInt();
                    Message clientMessage = getDecodedMessage(clientMessageLength);
                    processClientMessage(clientMessage);
                } catch (IOException ex) {
                    //Logger.getLogger(ServerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    private class UDPPacketListener implements Runnable{
        @Override
        public void run() {
            
            
        }
    } 
    
    //Getters
    public String getUsername(){
        return this.clientInformation.getUsername();
    }
    
    public Socket getCommunicationSocket(){
        return this.clientInformation.getCommunicationSocket();
    }
    
    public ClientInformation getClientInformation(){
        return this.clientInformation;
    }
    
}
