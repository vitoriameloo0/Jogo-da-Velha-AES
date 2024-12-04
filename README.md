# Jogo-da-Velha-AES

Esse é um projeto de um jogo da velha  com criptografia AES e comunicação via UDP

> :construction: O projeto necessita de melhorias no processo de descriptografia das mensagens recebidas, que atualmente possuem tamanhos incompatíveis com o requisito de serem múltiplos de 16 :construction:

## Comandos para rodar o programa no prompt, ja estando todos os prompt no diretorio que esta o programa:

Abrir um prompt pro servidor: 
```
javac UDPServer.java 
javac JogoDaVelha.java 
java UDPServer.java
```
Abrir prompt pro cliente 1:
```
javac UDPServer.java		(caso esteja em um computador diferente da que esta o servidor aberto)
javac JogoDaVelha.java 	(caso esteja em um computador diferente da que esta o servidor aberto)
java JogoDaVelha.java X
```
Abrir prompt pro cliente 2: 
```
javac UDPServer.java		(caso esteja em um computador diferente da que esta o servidor aberto)
javac JogoDaVelha.java 	(caso esteja em um computador diferente da que esta o servidor aberto)
java JogoDaVelha.java O
```
