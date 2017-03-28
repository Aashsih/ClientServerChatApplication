/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server_client_message_protocol;

import enums.MessageType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aashi
 */
public class PersonalMessage extends TCPMessage{
    private String sendTo;
    
    public PersonalMessage(String sentBy, String sendTo, String message) {
        super(sentBy, message);
        this.messageType = MessageType.PERSONAL_MESSAGE;
        this.sendTo = sendTo;
    }
    
    //Getter
    public String getSendTo() {
        return sendTo;
    }
    
    
    @Override
    public byte[] getEncapsulatedMessage() {
        if(sendTo == null){
            return null;
        }
        return super.getEncapsulatedMessage();
    }
}
