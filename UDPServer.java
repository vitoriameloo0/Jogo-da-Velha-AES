package Game; 


import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class UDPServer {
    private static List<InetAddress> clientAddresses = new ArrayList<InetAddress>();
    private static List<Integer> clientPorts = new ArrayList<Integer>();
    private static boolean turnoX = true; // Turno do jogador X
    
    private static final String AES_ALGORITHM = "AES";
    private static final byte[] AES_KEY_BYTES = "chave123456789012".getBytes(); // Chave de 16 bytes
    private static final SecretKey aesKey = new SecretKeySpec(AES_KEY_BYTES, AES_ALGORITHM);


    public static void main(String[] args) throws Exception {
        DatagramSocket aSocket = null;
        
        try {
            aSocket = new DatagramSocket(6789);
            byte[] buffer = new byte[1000];
            
            // Loop do servidor que ira ficar recebendo os pacotes
            while (true) {
            	// Receber um pacote UDP e armazenar os dados no buffer
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                
                // Extrair o endereço IP e a porta do cliente a partir do pacote recebido
                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();
                
                // Se o cliente ainda não estiver na lista de endereços e portas ele é adicionado
                if (!clientAddresses.contains(clientAddress) || !clientPorts.contains(clientPort)) {
                    clientAddresses.add(clientAddress);
                    clientPorts.add(clientPort);
                    System.out.println("Novo cliente registrado: " + clientAddress + ":" + clientPort); // Imprime o novo cliente
                    
                }
                
                // Descriptografar a mensagem recebida
                String receivedMessage = decrypt(new String(request.getData(), 0, request.getLength()));
                System.out.println("Mensagem recebida: " + receivedMessage);
                System.out.println("Mensagem recebida (tamanho): " + receivedMessage.length());


                
                // Processamento de comandos
                if (receivedMessage.equals("NOVO_JOGO") || receivedMessage.equals("ZERAR_PLACAR")) {
                	System.out.println("Comando recebido: " + receivedMessage);
                    broadcastMessage(receivedMessage, aSocket);
                    turnoX = true; // Reinicia para o jogador X
                    notificarTurno();
                    
                
                } else if (receivedMessage.startsWith("ATUALIZAR_PLACAR:")) {
                	System.out.println("Comando de atualização de placar recebido: " + receivedMessage);
                	broadcastMessage(receivedMessage, aSocket);
                
                }else if (receivedMessage.startsWith("VITORIA:")) {
                	System.out.println("Comando de vitória recebido: " + receivedMessage); 
                	broadcastMessage(receivedMessage, aSocket);
                } 

                else {
                    // Adicionar turno do jogador
                    String[] parts = receivedMessage.split(":");
                    int index = Integer.parseInt(parts[0]);
                    String player = parts[1];
                    System.out.println("Jogador " + player + " fez um movimento na posição " + index);
                    
                    String updatedMessage = index + ":" + player;

                    for (int i = 0; i < clientAddresses.size(); i++) {
                        byte[] m = updatedMessage.getBytes();
                        DatagramPacket reply = new DatagramPacket(m, m.length, clientAddresses.get(i), clientPorts.get(i));
                        aSocket.send(reply);
                    }

                    turnoX = !turnoX; // Alternar turno
                    notificarTurno();
                }
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null) aSocket.close();
        }
    }
    
    private static void broadcastMessage(String message, DatagramSocket socket) throws Exception {
        for (int i = 0; i < clientAddresses.size(); i++) {
            byte[] encryptedMessage = encrypt(message).getBytes();
            DatagramPacket reply = new DatagramPacket(encryptedMessage, encryptedMessage.length, clientAddresses.get(i), clientPorts.get(i));
            socket.send(reply);
        }
    }
    
    private static String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private static String decrypt(String encryptedText) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }

    
    
    private static void notificarTurno() {
        String message = "SUA_VEZ";
        try {
        	byte[] encryptedMessage = encrypt(message).getBytes();
            if (turnoX) {
            	System.out.println("É a vez do jogador X.");
                DatagramPacket turnoXPacket = new DatagramPacket(encryptedMessage, encryptedMessage.length, clientAddresses.get(0), clientPorts.get(0));
                DatagramSocket socket = new DatagramSocket();
                socket.send(turnoXPacket);
                socket.close();
            } else {
            	System.out.println("É a vez do jogador O.");
                DatagramPacket turnoOPacket = new DatagramPacket(encryptedMessage, encryptedMessage.length, clientAddresses.get(1), clientPorts.get(1));
                DatagramSocket socket = new DatagramSocket();
                socket.send(turnoOPacket);
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}