/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server_client_message_protocol;

/**
 *
 * @author aashi
 */
public class TCPMessage extends Message{
    
    public TCPMessage(String sentBy, String message) {
        super(sentBy, message);
    }
    
}
