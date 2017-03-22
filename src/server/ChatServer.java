/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import server_client_message_protocol.ConnectMessage;
import server_client_message_protocol.DisconnectMessage;
import server_client_message_protocol.UDPMessage;

/**
 *
 * @author aashi
 */
public class ChatServer {
    public static final int PORT_NUMBER = 8765;
    public static final String MULTICAST_ADDRESS = "224.2.2.3";
    private static final int UDP_UPDATE_TIME = 5000; // time in milliseconds
    
    private static ServerSocket chatServerSocket;
    private static MulticastSocket serverDatagramSocket;
    
    private static Map<String, ClientInformation> connectedClientToClientInformation = new HashMap<>();
    
    public static String getServerIPAddress() throws UnknownHostException{
        return InetAddress.getLocalHost().getHostAddress();
    }
    
    public static Map<String, ClientInformation> getConnectedClientAndSockets(){
        return (connectedClientToClientInformation == null)? null : Collections.unmodifiableMap(connectedClientToClientInformation);
    }
    
    public static boolean isUsernameAcceptable(String username){
        if(connectedClientToClientInformation != null){
            return !connectedClientToClientInformation.containsKey(username);
        }
        return false;
    }
   
    public static boolean addClientToClientSocketMap(ServerCommunicator communicationChannel){
        if(communicationChannel != null){
           connectedClientToClientInformation.put(communicationChannel.getUsername().toLowerCase(), communicationChannel.getClientInformation());
           return true;
        }
        return false;
    }
    
    public static void removeClientFromClientToSocketMap(String username){
       if(username != null){
            ClientInformation removedClient = connectedClientToClientInformation.remove(username.toLowerCase());
            removedClient = null;
        }
    }
    
    public static byte[] getAvailableClientsBytes(){
        ObjectOutputStream output = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            output = new ObjectOutputStream(bos);
            output.writeObject(new ArrayList<>(ChatServer.getConnectedClientAndSockets().keySet()));
            return bos.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(ServerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                output.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    public static void sendDisconnectMessage(DisconnectMessage message){
        
        if(serverDatagramSocket == null || message == null){
            return;
        }
        ChatServer.removeClientFromClientToSocketMap(message.getSentBy());
        List<Object> messageObject = new ArrayList<>();
        messageObject.add(message);
        messageObject.add(new ArrayList<>(ChatServer.getConnectedClientAndSockets().keySet()));
        sendUDPDatagramPacket(messageObject);

//            byte[] disconnectMessageBytes = message.getEncapsulatedMessage();
//            DatagramPacket disconnectMessagePacket = new DatagramPacket(disconnectMessageBytes, disconnectMessageBytes.length, InetAddress.getByName(MULTICAST_ADDRESS), PORT_NUMBER);
//            serverDatagramSocket.send(disconnectMessagePacket);
//            byte[] availableClientListBytes = getAvailableClientsBytes();
//            DatagramPacket messagePacket = new DatagramPacket(availableClientListBytes, availableClientListBytes.length, InetAddress.getByName(MULTICAST_ADDRESS), PORT_NUMBER);
//            serverDatagramSocket.send(messagePacket);
            

            //Since UDP messages arent guranteed to be received in order the following cant be done
            //sendUDPMessageLengthPacket(clientMessage.length);
//            for(ClientInformation aClient : ChatServer.getConnectedClientAndSockets().values()){
//                if(!aClient.getUsername().equalsIgnoreCase(message.getSentBy())){
//                    DatagramPacket messagePacket = new DatagramPacket(availableClientListBytes, availableClientListBytes.length, aClient.getClientIP(), aClient.getClientPort());
//                    serverDatagramSocket.send(messagePacket);
//                }
//            }
        
    }
    
    private static void sendUDPDatagramPacket(Object message){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream ouput = new ObjectOutputStream(bos);
            ouput.writeObject(message);
            byte[] messageBytes = bos.toByteArray();
            DatagramPacket messagePacket = new DatagramPacket(messageBytes, messageBytes.length, InetAddress.getByName(MULTICAST_ADDRESS), PORT_NUMBER);
            serverDatagramSocket.send(messagePacket);
        } catch (IOException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void sendConnectMessage(ConnectMessage message){
       
        if(serverDatagramSocket == null || message == null){
            return;
        }
        List<Object> messageObject = new ArrayList<>();
        messageObject.add(message);
        messageObject.add(new ArrayList<>(ChatServer.getConnectedClientAndSockets().keySet()));
        sendUDPDatagramPacket(messageObject);
            
            
//            byte[] connectMessageBytes = message.getEncapsulatedMessage();
//            DatagramPacket connectMessagePacket = new DatagramPacket(connectMessageBytes, connectMessageBytes.length, InetAddress.getByName(MULTICAST_ADDRESS), PORT_NUMBER);
//            serverDatagramSocket.send(connectMessagePacket);
//            byte[] availableClientListBytes = getAvailableClientsBytes();
//            DatagramPacket messagePacket = new DatagramPacket(availableClientListBytes, availableClientListBytes.length, InetAddress.getByName(MULTICAST_ADDRESS), PORT_NUMBER);
//            serverDatagramSocket.send(messagePacket);
            

            //Since UDP messages arent guranteed to be received in order the following cant be done
            //sendUDPMessageLengthPacket(clientMessage.length);
//            for(ClientInformation aClient : ChatServer.getConnectedClientAndSockets().values()){
//                if(!aClient.getUsername().equalsIgnoreCase(message.getSentBy())){
//                    DatagramPacket messagePacket = new DatagramPacket(availableClientListBytes, availableClientListBytes.length, aClient.getClientIP(), aClient.getClientPort());
//                    serverDatagramSocket.send(messagePacket);
//                }
//            }
       
    }
        
//    private static void sendConnectMessage(){
//        try {
//            if(serverDatagramSocket == null){
//                return;
//            }
//            byte[] clientMessage = getUDPConnectMessage();
//            //Since UDP messages arent guranteed to be received in order the following cant be done
//            //sendUDPMessageLengthPacket(clientMessage.length);
//            for(ClientInformation aClient : ChatServer.getConnectedClientAndSockets().values()){
//                DatagramPacket messagePacket = new DatagramPacket(clientMessage, clientMessage.length, aClient.getClientIP(), aClient.getClientPort());
//                serverDatagramSocket.send(messagePacket);
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(ServerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    
    //Since UDP messages arent guranteed to be received in order the following cant be done
    private static void sendUDPMessageLengthPacket(int messageLength){
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(UDPMessage.NUMBER_OF_BYTES_TO_READ_MESSAGE_LENGTH);
            byteBuffer.putInt(messageLength);
            DatagramPacket messageLengthPacket = new DatagramPacket(byteBuffer.array(), byteBuffer.array().length);
            serverDatagramSocket.send(messageLengthPacket);
        } catch (IOException ex) {
            Logger.getLogger(ServerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ChatServer(){
        try{
            chatServerSocket = new ServerSocket(PORT_NUMBER);
            serverDatagramSocket = new MulticastSocket();
            new java.util.Timer().scheduleAtFixedRate(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            List<Object> messageObject = new ArrayList<>();
                            messageObject.add(new ArrayList<>(ChatServer.getConnectedClientAndSockets().keySet()));
                            sendUDPDatagramPacket(messageObject);
                            System.out.println("UDP Packet sent after timer");
                        }
                    },0, UDP_UPDATE_TIME
            );
        } catch (IOException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("A socket could not be created for the server");
        }
    }
    
    public void stopServer(){
        chatServerSocket = null;
        connectedClientToClientInformation = null;
    }
    
    public void startChatServers(){
        new Thread(new TCPChatServer()).start();
        //new Thread(new UDPChatServer()).start();
    }
    
    private class TCPChatServer implements Runnable{

        @Override
        public void run() {
            if(chatServerSocket != null){
            while(true){
                try {
                    Socket communicationSocket = chatServerSocket.accept();
                    if(communicationSocket != null && serverDatagramSocket != null){
                        ServerCommunicator clientCommunicator = new ServerCommunicator(communicationSocket);
                    }
                    
                } catch (IOException ex) {
                    Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        }
        
    }
    
    private class UDPChatServer implements Runnable{

        @Override
        public void run() {
            while(true){
                try {
                    byte[] clientRegistrationRequet = new byte[UDPMessage.MAX_UDP_MESSAGE_SIZE];
                    DatagramPacket clientRegistrationRequetPacket = new DatagramPacket(clientRegistrationRequet, clientRegistrationRequet.length);
                    serverDatagramSocket.receive(clientRegistrationRequetPacket);
                    String registrationUsername = new String(clientRegistrationRequetPacket.getData());
                    String truncatedUsername = "";
                    for(int i = 0; i < registrationUsername.length() && registrationUsername.charAt(i) != 0; i++){
                        truncatedUsername += registrationUsername.charAt(i);
                    }
                    ClientInformation clientInformation = connectedClientToClientInformation.get(truncatedUsername.toLowerCase());
                    clientInformation.setClientIP(clientRegistrationRequetPacket.getAddress());
                    clientInformation.setClientPort(clientRegistrationRequetPacket.getPort());
                    //sendConnectMessage();
                } catch (IOException ex) {
                    Logger.getLogger(ServerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
    
//    public static void main(String[] args){
//        ChatServer chatServer = new ChatServer();
//        chatServer.startChatServers();
//    }
}
