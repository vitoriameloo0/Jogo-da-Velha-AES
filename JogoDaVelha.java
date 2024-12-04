package Game;

import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;


public class JogoDaVelha extends JFrame {
	// Configurações da tela
    JButton[] bt = new JButton[9];
    JButton novo = new JButton("Novo Jogo");
    JButton zerar = new JButton("Zerar Placar");
    JLabel placar = new JLabel("PLACAR");
    JLabel px = new JLabel("X -> 0");
    JLabel po = new JLabel("O -> 0");

    int PX = 0;
    int PO = 0;
    boolean[] click = new boolean[9];
    boolean minhaVez = false;
    String jogadorSimbolo;

    // Configuracoes para o socket
    DatagramSocket socket;
    InetAddress serverAddress;
    int serverPort = 6789;

    // Variaveis para a criptografia
    public String encriptada = "";
	public String aEncriptar = "";
	String chaveSimetrica = "Chave12345678901";
	
    public JogoDaVelha(final String jogadorSimbolo) {
        this.jogadorSimbolo = jogadorSimbolo;

        try {
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName("localhost"); // Endereço do servidor 
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Definir a tela
        setVisible(true);
        setTitle("Jogo da Velha");
        setDefaultCloseOperation(3);
        setLayout(null);
        setBounds(500, 300, 700, 500); // Para definir o tamanho da janela

        add(placar);
        add(px);
        add(po);
        placar.setBounds(450, 50, 100, 30);
        px.setBounds(430, 75, 140, 50);
        po.setBounds(480, 75, 140, 50);

        add(novo);
        add(zerar);
        novo.setBounds(410, 130, 140, 30);
        zerar.setBounds(410, 180, 140, 30);

        novo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                limpar();
                enviarMensagem("NOVOJOGO");
            }
        });

        zerar.addActionListener(new java.awt.event.ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                PO = 0;
                PX = 0;
                atualizar();
                enviarMensagem("ZERARPLACAR");
            }
        });

        // Criar os botões para simbolizar cada campo
        criarBotao(bt);
        // Inicializar o click como falso
        inicializarClick(click);
        
        // Criar acao nos 9 botoes
        for (int i = 0; i < 9; i++) {
            final int index = i;
            bt[i].addActionListener(new java.awt.event.ActionListener() {
               
                public void actionPerformed(ActionEvent e) {
                    if (click[index] == false && minhaVez) {
                        click[index] = true;
                        bt[index].setText(jogadorSimbolo);
                     
                        minhaVez = false;
                        enviarJogada(index, jogadorSimbolo);
                        ganhou();
                    }
                }
            });
        }

        // Thread para receber jogadas do servidor
        new Thread(() -> {
            while (true) {
                try {
                    byte[] buffer = new byte[1000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    socket.receive(reply);
                    String message = new String(reply.getData(), 0, reply.getLength());                  
                    processarMensagem(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void criarBotao(JButton[] bt) {
        int cont = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                bt[cont] = new JButton();
                add(bt[cont]);
                bt[cont].setBounds((100 * i) + 50, (100 * j) + 50, 95, 95); // Para definir o tamanho dos botões
                bt[cont].setFont(new Font("Arial", Font.BOLD, 40)); // Fonte do que vai ser escrito no botao
                cont++;
            }
        }
    }

    public void inicializarClick(boolean[] click) {
        for (int i = 0; i < 9; i++) {
            click[i] = false;
        }
    }

    public void atualizar() {
        px.setText("X -> " + PX);
        po.setText("O -> " + PO);
    }

    public void ganhou() {
        int cont = 0;

        for (int i = 0; i < 9; i++) {
            if (click[i] == true) 
                cont++;           
        }

        if (verificarVitoria("X")) {
            PX++;
            atualizar();
            limpar();
            enviarMensagem("VITORIA:" + "X");        
            enviarMensagem("ATUALIZARPLACAR:X:" + PX + ":O:" + PO);
            enviarMensagem("NOVOJOGO");
            
        } else if (verificarVitoria("O")) {
            PO++;
            atualizar();
            limpar();
            enviarMensagem("VITORIA:" + "O"); 
            enviarMensagem("ATUALIZARPLACAR:X:" + PX + ":O:" + PO);
            enviarMensagem("NOVOJOGO");
            
        } else if (cont == 9) {
            JOptionPane.showMessageDialog(null, "Empate");
            limpar();
            enviarMensagem("NOVOJOGO");
        }
    }
    
    
    public boolean verificarVitoria(String simbolo) {
    	return (bt[0].getText().equals(simbolo) && bt[1].getText().equals(simbolo) && bt[2].getText().equals(simbolo)) ||
    	           (bt[3].getText().equals(simbolo) && bt[4].getText().equals(simbolo) && bt[5].getText().equals(simbolo)) ||
    	           (bt[6].getText().equals(simbolo) && bt[7].getText().equals(simbolo) && bt[8].getText().equals(simbolo)) ||
    	           (bt[0].getText().equals(simbolo) && bt[3].getText().equals(simbolo) && bt[6].getText().equals(simbolo)) ||
    	           (bt[1].getText().equals(simbolo) && bt[4].getText().equals(simbolo) && bt[7].getText().equals(simbolo)) ||
    	           (bt[2].getText().equals(simbolo) && bt[5].getText().equals(simbolo) && bt[8].getText().equals(simbolo)) ||
    	           (bt[0].getText().equals(simbolo) && bt[4].getText().equals(simbolo) && bt[8].getText().equals(simbolo)) ||
    	           (bt[2].getText().equals(simbolo) && bt[4].getText().equals(simbolo) && bt[6].getText().equals(simbolo));
    }
    

    public void limpar() {
        for (int i = 0; i < 9; i++) {
            bt[i].setText("");
            click[i] = false;
        }
        minhaVez = (jogadorSimbolo.equals("X")) ? true : false; 
    }

    public void enviarJogada(int index, String player) {
        enviarMensagem(index + ":" + player);
    }

    public void enviarMensagem(String message) {
        try {                     
            String mensagemCriptografada = Encriptar(message, chaveSimetrica);
            
            // Converte a mensagem criptografada para bytes e enviar
            byte[] m = mensagemCriptografada.getBytes();
            DatagramPacket request = new DatagramPacket(m, m.length, serverAddress, serverPort);
            socket.send(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public void processarMensagem(String message) {
        try{                 
            String mensagemOriginal = Decriptar(message, chaveSimetrica);

            if (mensagemOriginal.equals("NOVOJOGO")) {      
                limpar();
                
            } else if (mensagemOriginal.equals("ZERARPLACAR")) {        	
                PO = 0;
                PX = 0;
                atualizar();
                
            } else if (mensagemOriginal.startsWith("ATUALIZARPLACAR:")) {       	
                String[] parts = mensagemOriginal.split(":");
                PX = Integer.parseInt(parts[2]);
                PO = Integer.parseInt(parts[4]);
                atualizar();
                
            } else if (mensagemOriginal.equals("SUAVEZ")) {        	
                minhaVez = true;
                
            } else if (mensagemOriginal.startsWith("VITORIA:")) {       
                String[] parts = mensagemOriginal.split(":");
                String vencedor = parts[1].trim();               
                JOptionPane.showMessageDialog(null, vencedor + " ganhou");

            }
            else {
                String[] parts = mensagemOriginal.split(":");
 
                if(parts.length > 1 && isNumeric(parts[0])) {                    
                    int index = Integer.parseInt(parts[0]);
                    String player = parts[1]; 
                    bt[index].setText(player);
                    click[index] = true;
                    minhaVez = jogadorSimbolo.equals(player) ? false : true;                    
                }  
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private boolean isNumeric(String str) {
    	try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // Cria uma chave criptografada  
 	public SecretKeySpec CriarChave(String chaveSimetrica) {
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
 	public String Encriptar(String encriptar, String chaveSimetrica) {
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
 	public String Decriptar(String decriptar, String chaveSimetrica) {
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

    public static void main(String[] args) {
        String jogadorSimbolo = args.length > 0 ? args[0] : "X";
        new JogoDaVelha(jogadorSimbolo);
    }
}
