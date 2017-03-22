/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server_client_message_protocol;

import enums.MessageType;



/**
 *
 * @author aashi
 */
public class BroadcastMessage extends TCPMessage{

    
    public BroadcastMessage(String sentBy, String message) {
        super(sentBy, message);
        this.messageType = MessageType.BROADCAST_MESSAGE;
    }
    
}
