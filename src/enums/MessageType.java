/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package enums;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author aashi
 */
public enum MessageType {
    PERSONAL_MESSAGE ("Personal Message"), BROADCAST_MESSAGE ("Broadcast Message"), DISCONNECT_MESSAGE ("Disconnected"), CONNECT_MESSAGE ("Connected");
    
    private static Map<String, MessageType> valueToMessageType = new HashMap<>();

    static {
        for(MessageType messageType : MessageType.values()){
            valueToMessageType.put(messageType.value,messageType);
        }
    }

    public static MessageType getCardiacPhase(String value){
        return valueToMessageType.get(value);
    }

    private String value;

    private MessageType(String value){
        this.value = value;
    }

    @Override
    public String toString(){
        return this.value;
    }
}
