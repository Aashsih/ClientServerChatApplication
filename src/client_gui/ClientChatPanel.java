/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client_gui;

import client.ChatClient;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import server_client_message_protocol.BroadcastMessage;
import server_client_message_protocol.Message;
import server_client_message_protocol.PersonalMessage;
import server_client_message_protocol.UDPMessage;

/**
 *
 * @author aashi
 */
public class ClientChatPanel extends javax.swing.JFrame {
    private ChatClient chatSession;
    
    /**
     * Creates new form ClientChatPanel
     */
    public ClientChatPanel() {
        if(createAndRegisterClient()){
            initComponents(); 
            chatSession.startMessageExchangeProtocol();
            new Thread(new AvailableClientsListUpdater()).start();
            new Thread(new ServerMessageReceiver()).start();
        }
        else{
            System.exit(0);
        }
        this.setResizable(false);
        this.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e){
                    chatSession.disconnectUserFromChatServer();
                }
            });
    }
    
    private void showMessage(Message message){
        JOptionPane.showConfirmDialog(this, message.getMessage(), null, JOptionPane.PLAIN_MESSAGE);
    }
    
    private boolean createAndRegisterClient(){
       
            JTextField txtUsername = new JTextField();
            JTextField txtServerIP = new JTextField();
            final JComponent[] inputs = new JComponent[] {
                    new JLabel("User Name"),
                    txtUsername,
                    new JLabel("Chat Server IP"),
                    txtServerIP,
            };
            boolean usernameAccepted = false;
            do{
                try {
                    int result = JOptionPane.showConfirmDialog(null, inputs, "Enter Deatils to join the chat", JOptionPane.PLAIN_MESSAGE);
                    if (result == JOptionPane.OK_OPTION) {
                        if(chatSereverDetailsEntered(txtUsername.getText(), txtServerIP.getText())){
                            chatSession = new ChatClient(txtServerIP.getText());
                            usernameAccepted = chatSession.registerClient(txtUsername.getText().toLowerCase());
                        }  
                    }
                    else if(result == JOptionPane.CLOSED_OPTION){
                        //Send disconnect message
                        System.exit(0);
                    }
                    if(!usernameAccepted){
                        chatSession = null;
                        JOptionPane.showMessageDialog(null, "User Name not approved. Please try another one");   
                    }
                } catch (Exception ex) {
                    //Logger.getLogger(ClientChatPanel.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("The server is down, please try again later");
                    JOptionPane.showMessageDialog(null, "The server is down or does not exist, please try again later");   
                }
            }while(!usernameAccepted);
        
        this.setTitle(chatSession.getUsername());
        return true;
    }

    private boolean chatSereverDetailsEntered(String username, String serverIP){
        return (username.isEmpty() || serverIP.isEmpty()) ? false : true;
    }
    
    private void restartApplication(){
        chatSession = null;
        this.setVisible(false);
        if(createAndRegisterClient()){
            initComponents();
            this.setVisible(true);
            chatSession.startMessageExchangeProtocol();
            new Thread(new AvailableClientsListUpdater()).start();
            new Thread(new ServerMessageReceiver()).start();
        }
        else{
            System.exit(0);
        }
    }
    
    private void notifyAvailableClientsHasChnaged(){
        //Store the selected value before updating the list
        String selectedValue = this.listAvailableClients.getSelectedValue();
        //Update the list
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String availableClinet : chatSession.getAvailableClients()) {
            model.addElement(availableClinet);
        }    
        this.listAvailableClients.setModel(model);
        //If the previously selected value is still in the list, then make that the current selected value
        if(model.contains(selectedValue)){
            int index = model.indexOf(selectedValue);
            this.listAvailableClients.setSelectedIndex(index);
        }
    }
    
    private void processServerMessage(Message message){
        System.out.println("Processing Server Message");
        if(message instanceof PersonalMessage){
            chatSession.appendPersonalMessage((PersonalMessage) message);
        }
        else if(message instanceof BroadcastMessage){
            if(!chatSession.isMessageSentByCurrentClient(message)){
                showBroadcastMessage((BroadcastMessage) message);
            }
        }
        if(listAvailableClients.getSelectedValue() != null){
            txtAreaChatHistory.setText(chatSession.getClientChatHistory(listAvailableClients.getSelectedValue().toLowerCase()).toString());
        }
    }
    
    private void showBroadcastMessage(BroadcastMessage message){
        JOptionPane.showConfirmDialog(this, message.getMessage(), message.getSentBy() + " says:" , JOptionPane.PLAIN_MESSAGE);
    }
    
    private class AvailableClientsListUpdater implements Runnable{

        @Override
        public void run() {
            while(chatSession.isConnectedToChat()){
                try {
                    //the followin method should thow an exception incase the connection was interrupted
                    Message messageReceived = chatSession.listenForAvailableClientsListUpdate(ClientChatPanel.this);
                    notifyAvailableClientsHasChnaged();
                    if(messageReceived != null){
                        showMessage(messageReceived);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ClientChatPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ClientChatPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
    
    private class ServerMessageReceiver implements Runnable{

        @Override
        public void run() {
            try {
                //Before starting to receive info, send the Server the client information in a dummy packet
                chatSession.sendConnectMessage();
                while(chatSession.isConnectedToChat()){
                    try {
                        Message message = chatSession.getServerMessages();
                        System.out.println("Message received");
                        processServerMessage(message);
                    } catch (IOException ex) {
                        //Logger.getLogger(ClientChatPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ClientChatPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    private void sendMessage(){
        String message = txtMessageField.getText();
        if(message == null || message.isEmpty() || chatSession == null || listAvailableClients.getSelectedValue() == null){
            return;
        }
        chatSession.sendPersonalMessage(message, listAvailableClients.getSelectedValue());
        txtAreaChatHistory.setText(chatSession.getClientChatHistory(listAvailableClients.getSelectedValue().toLowerCase()).toString());
        txtMessageField.setText("");
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator1 = new javax.swing.JSeparator();
        btnBroadcast = new javax.swing.JButton();
        txtMessageField = new javax.swing.JTextField();
        btnSendMessage = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        listAvailableClients = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtAreaChatHistory = new javax.swing.JTextArea();
        btnLogout = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        btnBroadcast.setText("Braodcast Message");
        btnBroadcast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBroadcastActionPerformed(evt);
            }
        });

        txtMessageField.setToolTipText("");

        btnSendMessage.setText("Send");
        btnSendMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendMessageActionPerformed(evt);
            }
        });

        listAvailableClients.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        listAvailableClients.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listAvailableClients.setFixedCellHeight(40);
        listAvailableClients.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listAvailableClientsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(listAvailableClients);

        txtAreaChatHistory.setEditable(false);
        txtAreaChatHistory.setBackground(new java.awt.Color(200, 200, 200));
        txtAreaChatHistory.setColumns(20);
        txtAreaChatHistory.setRows(5);
        jScrollPane2.setViewportView(txtAreaChatHistory);

        btnLogout.setText("Logout");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane1)
                        .addComponent(btnLogout, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE))
                    .addComponent(btnBroadcast, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtMessageField, javax.swing.GroupLayout.PREFERRED_SIZE, 703, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSendMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE))
                    .addComponent(jScrollPane2)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtMessageField, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnSendMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnBroadcast, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    private void btnSendMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendMessageActionPerformed
        sendMessage();
    }//GEN-LAST:event_btnSendMessageActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", null, JOptionPane.YES_NO_OPTION);
        if(result == JOptionPane.YES_OPTION){
            chatSession.disconnectUserFromChatServer();
            restartApplication();
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnBroadcastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBroadcastActionPerformed
        JTextField txtBroadcastMessage = new JTextField();
        final JComponent[] inputs = new JComponent[] {
                new JLabel("Message"),
                txtBroadcastMessage,
        };
        int result = JOptionPane.showConfirmDialog(this, inputs, "Enter a broadcast message", JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            //Send Braodcast Message
            chatSession.sendBraodcastMessage(txtBroadcastMessage.getText());
            System.out.println("Request server to send a broadcast Message");
        }
    }//GEN-LAST:event_btnBroadcastActionPerformed

    private void listAvailableClientsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listAvailableClientsValueChanged
        if(listAvailableClients.getSelectedValue() != null){
            txtAreaChatHistory.setText(chatSession.getClientChatHistory(listAvailableClients.getSelectedValue().toLowerCase()).toString());
        }
    }//GEN-LAST:event_listAvailableClientsValueChanged

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ClientChatPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientChatPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientChatPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientChatPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ClientChatPanel().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBroadcast;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnSendMessage;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JList<String> listAvailableClients;
    private javax.swing.JTextArea txtAreaChatHistory;
    private javax.swing.JTextField txtMessageField;
    // End of variables declaration//GEN-END:variables
    
}
