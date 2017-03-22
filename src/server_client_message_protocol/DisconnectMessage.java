/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server_client_message_protocol;

import enums.MessageType;
import java.rmi.RemoteException;

/**
 *
 * @author aashi
 */
public class DisconnectMessage extends UDPMessage{
    private static final String DISCONNECT_MESSAGE = "left the chat room";
    
    public DisconnectMessage(String sentBy, String message) {
        super(sentBy, message);
        this.message = sentBy + " " + DISCONNECT_MESSAGE;
        this.messageType = MessageType.DISCONNECT_MESSAGE;
    }

    
}
