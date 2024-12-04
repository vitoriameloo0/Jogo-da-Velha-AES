package Game; 

import java.net.*;
import java.security.MessageDigest;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class UDPServer {
    private static List<InetAddress> clientAddresses = new ArrayList<InetAddress>();
    private static List<Integer> clientPorts = new ArrayList<Integer>();
    private static boolean turnoX = true; // Turno do jogador X
    
    
    private static String chaveSimetrica = "Chave12345678901";
    public String encriptada = "";
	public String aEncriptar = "";

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
                    System.out.println("Novo cliente registrado: " + clientAddress + ":" + clientPort + "\n"); // Imprime o novo cliente
                    
                }
                
                // A mensagem recebida é transformada em String               
                String mensagemRecebida = new String(request.getData(), 0, request.getLength());
                System.out.println("Mensagem recebida: " + mensagemRecebida);
                
                
                // Descriptografar a mensagem recebida
                String decryptedMessage = Decriptar(mensagemRecebida, chaveSimetrica);
                System.out.println("Mensagem descriptografada: " + decryptedMessage);
                
                
                if (decryptedMessage.equals("NOVOJOGO") || decryptedMessage.equals("ZERARPLACAR")) {
                    broadcastMessage(decryptedMessage, aSocket, request);
                    turnoX = true; // Reinicia para o jogador X
                    notificarTurno();
                    
                
                } else if (decryptedMessage.startsWith("ATUALIZARPLACAR:")) {                	
                    broadcastMessage(decryptedMessage, aSocket, request);
                
                    
                }else if (decryptedMessage.startsWith("VITORIA:")) {                 
                    broadcastMessage(decryptedMessage, aSocket, request);
                    
                } 
                else {
                    // Adicionar turno do jogador
                    String[] parts = decryptedMessage.split(":");
                    int index = Integer.parseInt(parts[0]);
                    String player = parts[1];
                    
                    System.out.println("Jogador " + player + " fez um movimento na posição " + index + "\n");
                    
                    String updatedMessage = index + ":" + player;
                    String encryptedMessage = Encriptar(updatedMessage, chaveSimetrica);

                    for (int i = 0; i < clientAddresses.size(); i++) {
                        byte[] m = encryptedMessage.getBytes();
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
    
    private static void broadcastMessage(String message, DatagramSocket aSocket, DatagramPacket request) throws Exception {
        for (int i = 0; i < clientAddresses.size(); i++) {   
            String encryptedMessage = null;
			try {
				encryptedMessage = Encriptar(message, chaveSimetrica);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}        
            DatagramPacket reply = new DatagramPacket(encryptedMessage.getBytes(), encryptedMessage.length(), clientAddresses.get(i), clientPorts.get(i));
            aSocket.send(reply);
        }
    }
    
    private static void notificarTurno() {
        String message = "SUAVEZ";
        try {
            byte[] m = message.getBytes();
            if (turnoX) {
            	System.out.println("É a vez do jogador X" + "\n");
                DatagramPacket turnoXPacket = new DatagramPacket(m, m.length, clientAddresses.get(0), clientPorts.get(0));
                DatagramSocket socket = new DatagramSocket();
                socket.send(turnoXPacket);
                socket.close();
            } else {
            	System.out.println("É a vez do jogador O" + "\n");
                DatagramPacket turnoOPacket = new DatagramPacket(m, m.length, clientAddresses.get(1), clientPorts.get(1));
                DatagramSocket socket = new DatagramSocket();
                socket.send(turnoOPacket);
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
 // Cria uma chave criptografada  
 	public static SecretKeySpec CriarChave(String chaveSimetrica) {
 		try {
 			// Converte para bytes a chave string
 			byte[] chave = chaveSimetrica.getBytes("Cp1252");		// Pode ser UTF-8; Mudar nas configuracoes do eclipse tambem
 			
 			// Criptografo com hash a chave
 			MessageDigest md = MessageDigest.getInstance("SHA-1");
 			chave = md.digest(chave);
 			
 			/* Com o copyOf, valida a chave para ser no minimo x bits (indicado), bits insuficientes ele completa. 
 			 * Se chave passar de 32 bits ele capitura somente x bits (indicado).	
 			 * */
 			chave = Arrays.copyOf(chave, 32); 
 			SecretKeySpec secretKeySpec = new SecretKeySpec(chave, "AES");
 			return secretKeySpec;
 			
 			/*System.out.println(new String(chave));
 			for (int i = 0;i< new String(chave).length(); i++) {
 				System.out.printf("[%d] %c\n",i,new String(chave).charAt(i));
 				
 			}*/
 		} catch (Exception e) {
 			// TODO: handle exception
 			System.err.println("\nErro ao criar chave: \n");
 			e.printStackTrace();
 			return null;
 		}
 		
 	}
 	
 	// Encriptar uma mensagem. Recebe mensagem e chave do tipo string. Retorna string criptografada.
 	public static String Encriptar(String encriptar, String chaveSimetrica) {
 		try {
 			SecretKeySpec secretKeySpec = CriarChave(chaveSimetrica);
 			
 			//Seleciona o algoritmo AES e a opcao de criptografar com a chave definida anteriormente
 			Cipher cipher = Cipher.getInstance("AES");
 			cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
 			
 			//Converte strinf para bytes, pois a classe cipher usa vetor de bytes
 			byte [] mensagem = encriptar.getBytes("Cp1252");
 			byte [] mensagemEncriptada = cipher.doFinal(mensagem);	//doFinal: Criptografa ou descriptografa dados em uma opera  o de parte  nica ou finaliza uma opera  o de v rias partes
 			
 			// converter de novo para string
 			String mensagemEncriptadaString  = Base64.getEncoder().encodeToString(mensagemEncriptada);
 			return mensagemEncriptadaString;
 			
 		} catch (Exception e) {
 			// TODO: handle exception
 			System.err.println("\nErro ao encriptar mensagem: \n");
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	// Decriptar uma mensagem. Recebe mensagem e chave do tipo string. Retorna string decriptografada.
 	public static String Decriptar(String decriptar, String chaveSimetrica) {
 		try {
 			SecretKeySpec secretKeySpec = CriarChave(chaveSimetrica);
 			Cipher cipher = Cipher.getInstance("AES");
 			
 			//Seleciona o algoritmo AES e a opcao de degriptografar com a chave definida anteriormente
 			cipher.init(Cipher.DECRYPT_MODE,secretKeySpec);
 			
 			byte [] mensagem = Base64.getDecoder().decode(decriptar);
 			byte [] mensagemDecriptada = cipher.doFinal(mensagem);
 			String mensagemDecriptadaString  = new String(mensagemDecriptada);
 			return mensagemDecriptadaString;
 			
 		} catch (Exception e) {
 			// TODO: handle exception
 			System.err.println("\nErro ao decriptar mensagem: \n");
 			e.printStackTrace();
 			return null;
 		}
 	}
}
