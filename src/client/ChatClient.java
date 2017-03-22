package client;


import client_gui.ClientChatPanel;
import enums.MessageType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import server_client_message_protocol.BroadcastMessage;
import server_client_message_protocol.ConnectMessage;
import server_client_message_protocol.DisconnectMessage;
import server_client_message_protocol.Message;
import server_client_message_protocol.PersonalMessage;
import server_client_message_protocol.UDPMessage;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author aashi
 */
public class ChatClient{
    private static final int PORT_NUMBER = 8765;
    public static final String MULTICAST_ADDRESS = "224.2.2.3";
    
    private Socket chatSocket;
    private String serverIP;
    private MulticastSocket clientDatagramSocket;
    private DataInputStream dis;
    private DataOutputStream dos; 
    private String username;
    private Map<String, StringBuffer> clientToChatHistory;
    private boolean connectedToChat;
    
    public ChatClient(String serverIP) throws Exception{
        if(serverIP == null || serverIP.isEmpty()){
            throw new Exception();
        }
        this.serverIP = serverIP;
        chatSocket = new Socket(this.serverIP, PORT_NUMBER);
        clientDatagramSocket = new MulticastSocket(PORT_NUMBER);
        clientDatagramSocket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
        dis = new DataInputStream(chatSocket.getInputStream());
        dos = new DataOutputStream(chatSocket.getOutputStream());
    }

    public boolean registerClient(String username){
        sendUsernameForVerification(username);
        try {
            System.out.println("Waiting for server acknowledgement");
            if(dis.readUTF().equalsIgnoreCase(Message.USERNAME_ACCEPTED)){
                System.out.println("Username accepted");
                this.username = username;
                return true;
            }
        } catch (IOException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }   
    
    public boolean isConnectedToChat(){
        return connectedToChat;
    }
    
    public boolean isMessageSentByCurrentClient(Message message){
        return (message == null)? false : message.getSentBy().equalsIgnoreCase(username);
    }
    
    public Message listenForAvailableClientsListUpdate(ClientChatPanel mainFrame) throws IOException, ClassNotFoundException{
        //Since UDP messages arent guranteed to be received in order the following cant be done
        //int packetLength = getUDPPacketLength();
        if(clientDatagramSocket == null){
            throw new IOException();
        }
        DatagramPacket receivablePacket = getReceivableDatagramPacket();
        clientDatagramSocket.receive(receivablePacket);
        Object receivedObject = getObjectFromBytes(receivablePacket.getData());
        List<Object> receivedObjectList = (List<Object>) receivedObject;
        if(receivedObjectList.size() > 1){
            updateClientToChatHistoryMap((List<String>) receivedObjectList.get(1));
            Message receivedMessage = (Message) receivedObjectList.get(0);
            if(isMessageSentByCurrentClient(receivedMessage)){
                return null;
            }
            return receivedMessage;
        }
        else{
            updateClientToChatHistoryMap((List<String>) receivedObjectList.get(0));
            return null;
        }    
//        if(receivedObject instanceof List){
//            //Notify user on the UI
//            mainFrame.showMessage((Message) receivedObject);
//            //The problem is that the server sends two packets and the 2nd one is lost
//            //the following code gets the same object as the above
//            
//            //Inorder to counter the above problem, the server can send a list of objects
//            //this list will contain two objects
//            //1. the message object
//            //2. the list of availbale clients
//            DatagramPacket availableClientListPacket = getReceivableDatagramPacket();
//            clientDatagramSocket.receive(availableClientListPacket);
//            Object availableClientList = getObjectFromBytes(receivablePacket.getData());
//            updateClientToChatHistoryMap((List<String>) availableClientList);
//        }
//        else{
//            updateClientToChatHistoryMap((List<String>) receivedObject);
//        }
    }
    
    private Object getObjectFromBytes(byte[] data) throws IOException, ClassNotFoundException{
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream input = new ObjectInputStream(bis);
        Object object = input.readObject();
        input.close();
        return object;
    }
    
    private DatagramPacket getReceivableDatagramPacket(){
        byte[] serverPacketBytes = new byte[UDPMessage.MAX_UDP_MESSAGE_SIZE];
        return new DatagramPacket(serverPacketBytes, serverPacketBytes.length);
    }
    
    public Set<String> getAvailableClients(){
        return this.clientToChatHistory.keySet();
    }
    
    public Message getServerMessages() throws IOException{
        int serverMessageLength = dis.readInt();
        return getDecodedMessage(serverMessageLength);
    }
    
    public void startMessageExchangeProtocol(){
        clientToChatHistory = new HashMap<>();
        connectedToChat = true;
    }
    
    public void appendPersonalMessage(PersonalMessage message){
        if(clientToChatHistory == null || message == null){
            return;
        }
        else if(message.getSentBy().equalsIgnoreCase(username)){
            if(clientToChatHistory.containsKey(message.getSendTo().toLowerCase())){
                appendMessageToChatHistory(message.getSendTo(), message.getSentBy(), message.getMessage());
            }
            else{
                clientToChatHistory.put(message.getSendTo().toLowerCase(), null);
                appendMessageToChatHistory(message.getSendTo(), message.getSentBy(), message.getMessage());   
            }
        }
        else{
            if(clientToChatHistory.containsKey(message.getSentBy().toLowerCase())){
                appendMessageToChatHistory(message.getSentBy(), message.getSentBy(), message.getMessage());
            }
            else{
                clientToChatHistory.put(message.getSentBy().toLowerCase(), null);
                appendMessageToChatHistory(message.getSentBy(), message.getSentBy(), message.getMessage());   
            }
        }
//        else if(clientToChatHistory.containsKey(message.getSentBy().toLowerCase())){
//            if(message.getSentBy().equalsIgnoreCase(username)){
//                appendMessageToChatHistory(message.getSendTo(), message.getSentBy(), message.getMessage());
//            }
//            else{
//                appendMessageToChatHistory(message.getSentBy(), message.getSentBy(), message.getMessage());
//            }
//        }
//        else{
//            if(message.getSentBy().equalsIgnoreCase(username)){
//                clientToChatHistory.put(message.getSendTo().toLowerCase(), null);
//                appendMessageToChatHistory(message.getSendTo(), message.getSentBy(), message.getMessage());
//            }
//            else{
//                clientToChatHistory.put(message.getSentBy().toLowerCase(), null);
//                appendMessageToChatHistory(message.getSentBy(), message.getSentBy(), message.getMessage());
//            }
//        }
    }
    
    public void disconnectUserFromChatServer(){
        try {
            connectedToChat = false;
            clientToChatHistory = null;
            sendDisconnectMessage();
            dis.close();
            dos.close();
            chatSocket.close();
            clientDatagramSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    public boolean sendPersonalMessage(String message, String sendTo){
        PersonalMessage personalMessage = new PersonalMessage(username, sendTo, message);
        appendPersonalMessage(personalMessage);
        sendMessageToServer(personalMessage); 
        return true;
    }
    
    public boolean sendBraodcastMessage(String message){
        BroadcastMessage broadcastMessage = new BroadcastMessage(username, message);
        sendMessageToServer(broadcastMessage);
        return true;
    }
    
    public boolean sendConnectMessage() throws UnknownHostException, IOException{
        ConnectMessage connectMessage = new ConnectMessage(username, "");
        sendMessageToServer(connectMessage);
        return true;
//        if(clientDatagramSocket == null){
//            throw new IOException();
//        }
//        byte[] usernameBytes = this.username.getBytes();
//        DatagramPacket registrationPacket = new DatagramPacket(usernameBytes, usernameBytes.length, InetAddress.getLocalHost(), ChatClient.PORT_NUMBER);
//        clientDatagramSocket.send(registrationPacket);
    }
    
    public StringBuffer getClientChatHistory(String clientName){
        if(clientName == null || !clientToChatHistory.containsKey(clientName)){
            return null;
        }
        return clientToChatHistory.get(clientName);
    }
    
    public String getUsername(){
        return this.username;
    }
    
    private void updateClientToChatHistoryMap(List<String> availableClientsList){
        //The remove should be done before the add to enhance efficiency
        //Removes the client that have disconnected
        availableClientsList.remove(username.toLowerCase());
        Set<String> availableClients = new HashSet<>(availableClientsList);
        //This copy is required to avoid any concurrent modificatiokn exception in the following loop
        //The above exception occurs as the list is beeing looped and modified (removed) at the same time
        Map<String, StringBuffer> clientToChatHistoryMapCopy = new HashMap<>();
        clientToChatHistoryMapCopy.putAll(clientToChatHistory);
        for(String currentClient : clientToChatHistoryMapCopy.keySet()){
            if(!availableClients.contains(currentClient.toLowerCase())){
                clientToChatHistory.remove(currentClient);
            }
        }
        //Adds the clients that have connected
        for(String availableClient : availableClients){
            if(!clientToChatHistory.containsKey(availableClient)){
                clientToChatHistory.put(availableClient, new StringBuffer());
            }  
        }
    }
    
    //Since UDP messages arent guranteed to be received in order the following cant be done
    private int getUDPPacketLength() throws IOException{
        byte[] serverPacketLengthBytes = new byte[DisconnectMessage.NUMBER_OF_BYTES_TO_READ_MESSAGE_LENGTH];
        DatagramPacket serverPacketLength = new DatagramPacket(serverPacketLengthBytes, serverPacketLengthBytes.length);
        clientDatagramSocket.receive(serverPacketLength);
        return ByteBuffer.wrap(serverPacketLength.getData()).getInt();
    }
    
    private boolean sendDisconnectMessage(){
        DisconnectMessage disconnectMessage = new DisconnectMessage(username, "");
        sendMessageToServer(disconnectMessage);
        return true;
    }
    
   private void sendMessageToServer(Message message){
        try {
            byte[] encapsulatedMessage = message.getEncapsulatedMessage();
            dos.writeInt(encapsulatedMessage.length);
            dos.write(encapsulatedMessage);
            System.out.println("Message sent to server");
        } catch (RemoteException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void sendUsernameForVerification(String username){
        try {
            dos.writeUTF(username);
            System.out.println("Sent username to server");
        } catch (IOException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Message getDecodedMessage(int messageLength){
        try {
            
            byte[] encapsulatedClientMessage = new byte[messageLength];
            dis.read(encapsulatedClientMessage);
            return Message.getDecodedMessage(encapsulatedClientMessage);
            
        } catch (IOException ex) {
            //Logger.getLogger(ServerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private void appendMessageToChatHistory(String clientName, String sentBy, String message){
        StringBuffer chatHistory = clientToChatHistory.get(clientName.toLowerCase());
        if(chatHistory == null){
            chatHistory = new StringBuffer();
            clientToChatHistory.put(clientName.toLowerCase(), chatHistory);
        }
        chatHistory.append(sentBy + " :\n" + message + "\n\n");
    }
}
