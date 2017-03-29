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
public class ConnectMessage extends UDPMessage{
    private static final String CONNECT_MESSAGE = "joined the chat room";
    
    public ConnectMessage(String sentBy, String message) {
        super(sentBy, message);
        this.message = sentBy + " " + CONNECT_MESSAGE;
        this.messageType = MessageType.CONNECT_MESSAGE;
    }
    
}
