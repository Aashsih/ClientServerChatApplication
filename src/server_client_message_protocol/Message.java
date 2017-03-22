package server_client_message_protocol;

import enums.MessageType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author aashi
 */
public abstract class Message implements IMessage, Serializable{
    /**
     * Note: 
     * A better abstraction would be to make two further abstract subclasses
     * 1. TCPMessage
     * 2. UDPMessage
     * The above can be implemented as an extension later on
     */
    public static final String USERNAME_ACCEPTED = "Username Accepted";
    public static final String USERNAME_NOT_ACCEPTED = "Username Not Accepted";
    protected static final String FIELD_SEPARATOR = ";";
    
    protected String sentBy;
    protected String message;
    protected MessageType messageType;
   
    public Message(String sentBy, String message){
        this.sentBy = sentBy;
        this.message = message;
    }

    public static Message getDecodedMessage(byte[] encapsulatedMessage){
        try {
            ObjectInputStream ois = null;
            ByteArrayInputStream bis = new ByteArrayInputStream(encapsulatedMessage);
            ois = new ObjectInputStream(bis);
            return (Message) ois.readObject();
        } catch (IOException ex) {
            Logger.getLogger(Message.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Message.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @Override
    public byte[] getEncapsulatedMessage() throws RemoteException {
        ByteArrayOutputStream bos = null;
        try {
            if(sentBy == null || message == null){
                return null;
            }
            bos = new ByteArrayOutputStream();
            ObjectOutput output = new ObjectOutputStream(bos);
            output.writeObject(this);
            output.close();
            return bos.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(PersonalMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getSentBy() {
        return sentBy;
    }

    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return "Message{" + "sentBy=" + sentBy + ", message=" + message + ", messageType=" + messageType + '}';
    }
}
