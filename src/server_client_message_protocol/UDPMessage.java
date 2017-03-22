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
public class UDPMessage extends Message{
    public static final int NUMBER_OF_BYTES_TO_READ_MESSAGE_LENGTH = Integer.SIZE/Byte.SIZE;
    public static final int MAX_UDP_MESSAGE_SIZE = 1024; // maximum number of bytes that can be send over UDP
    
    public UDPMessage(String sentBy, String message) {
        super(sentBy, message);
    }
    
}
