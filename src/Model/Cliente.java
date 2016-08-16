package Model;

import Model.Util.Sistema;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cliente implements Runnable {

    private Socket socketServidor;                          // socket para conexão com servidor
    private Socket socketCliente;                           // socket para transferencia de arquivos
    private ArrayList<Usuario> usuarios;                    // Lista de usuários frequentes (cache do cliente)
    private ArrayList<Arquivo> arquivos;                    // Lista com os arquivos
    private String ipServidor;                              // Ip do servidor   
    private int portaServidor;                              // Porta de conexão com o servidor
    private final String dirCacheUsuario = "cacheUser.ld";  // String contendo o diretório do arquivo de cache de usuarios
    private int menuAtual;                                  // Variável de controle sobre estado do sistema
    private Scanner entradaServidor;
    private PrintStream saidaServidor;
    private int auxLogInECadastro = 0;
    private String email, senha;

    public Cliente(String ipServidor, int portaServidor) {
        this.ipServidor = ipServidor;
        this.portaServidor = portaServidor;
    }

    @Override
    public void run() {
        try {
            socketServidor = new Socket(ipServidor, portaServidor);
            entradaServidor = new Scanner(socketServidor.getInputStream());
            saidaServidor = new PrintStream(socketServidor.getOutputStream());
            Menu(0);
            while (entradaServidor.hasNext()) {
                RecebeMsgServidor(entradaServidor.nextLine());
            }

        } catch (IllegalArgumentException ex) {
            System.out.println("Número de porta Inválido");
        } catch (SocketException ex) {
            System.out.println("Servidor Offline");
        } catch (IOException ex) {
            System.out.println("Erro Grave");
        }
    }

    public void RecebeMsgUsuario(String msg) throws IOException, ClassNotFoundException {
        /*  
            Codigos:
            #01 - CADASTRO
            #04 - LOG IN NORMAL
            #07 - LOG IN CACHE
            #08 - ATUALIZAR ARQUIVOS
            #09 - DESLOGAR
            #15 - LISTA DE ARQUIVOS
         */
        switch (menuAtual) {
            // Tratamento para o Menu Inicial
            case 0:
                switch (msg) {
                    case "1":
                        Menu(1);
                        break;
                    case "2":
                        Menu(2);
                }
                break;
            // Tratamento para o Log in
            case 1:
                if (auxLogInECadastro == 0) {
                    email = msg;
                    auxLogInECadastro++;
                    Menu(1);
                } else if (auxLogInECadastro == 1) {
                    senha = msg;
                    auxLogInECadastro--;
                    CarregarDadosLogIn();
                    Usuario aux = new Usuario(email, senha);
                    if (usuarios.contains(aux)) {
                        saidaServidor.print("#07:" + email);
                    } else {
                        saidaServidor.print("#04:" + email + ":" + senha);
                    }
                }
                break;
            // Tratamento para o Cadastro
            case 2:
                if (auxLogInECadastro == 0) {
                    email = msg;
                    auxLogInECadastro++;
                } else if (auxLogInECadastro == 1) {
                    senha = msg;
                    auxLogInECadastro--;
                    saidaServidor.print("#01:" + email + ":" + senha);
                }
                break;
            // Tratamento para o Menu de Navegação    
            case 3:
                break;
            // Tratamento para o Menu do Arquivo
            case 4:
                break;

        }
    }

    private void RecebeMsgServidor(String msg) {
        String[] mensagem = msg.split(":");
        /*  
            Codigos:
            #02 - CADASTRO SUCESSO
            #03 - CADASTRO ERRO
            #05 - LOG IN SUCESSO 
            #06 - LOG IN ERRO (0 - DADOS INVÁLIDOS / 1 - USUÁRIO LOGADO)
            #14 - MANDAR ARQUIVOS
         */

        switch (menuAtual) {
            // Tratamento para o Menu Inicial
            case 0:
                break;
            // Tratamento para o Log in
            case 1:
                if (mensagem[0].equals("#05")) {
                    System.out.println("Log in realizado com sucesso");
                    Menu(3);
                } else if (mensagem[0].equals("#06")) {
                    if (mensagem[1].equals("0")) {
                        System.out.println("Dados incorretos");
                    } else if (mensagem[1].equals("1")) {
                        System.out.println("O usuário já está logado");
                    }
                    Menu(0);
                }
                break;
            // Tratamento para o Cadastro
            case 2:
                if (mensagem[0].equals("#02")) {
                    System.out.println("Usuário cadastrado:" + email);
                } else if (mensagem[0].equals("#03")) {
                    System.out.println("Email já cadastrado");
                }
                Menu(0);
                break;
            // Tratamento para o Menu de Navegação 
            case 3:
                
                break;
            // Tratamento para o Menu do Arquivo
            case 4:
                break;
        }
    }

    private void Menu(int menu) {
        switch (menu) {
            case 0:
                menuAtual = 0;
                System.out.println("-----------------------------------------------");
                System.out.println("       Bem vindo ao Sistema lava duto");
                System.out.println("-----------------------------------------------");
                System.out.println("           Escolha uma das opções");
                System.out.println("( 1 ) Log In");
                System.out.println("( 2 ) Cadastrar Novo Usuário");
                System.out.println("-----------------------------------------------");
                break;
            case 1:
                menuAtual = 1;
                if (auxLogInECadastro == 0) {
                    System.out.println("Entre com o email do usuário");
                } else if (auxLogInECadastro == 1) {
                    System.out.println("Entre com a senha do usuário");
                }
                break;
            case 2:
                menuAtual = 2;
                if (auxLogInECadastro == 0) {
                    System.out.println("Entre com o email para cadastro");
                } else if (auxLogInECadastro == 1) {
                    System.out.println("Entre com a senha para cadastro");
                }
                break;
            case 3:
                menuAtual = 3;
                System.out.println("-----------------------------------------------");
                System.out.println("Lista de Arquivos");
                System.out.println("-----------------------------------------------");
                for (Arquivo arq : arquivos) {
                    System.out.println(arq.getName());
                }
                System.out.println("-----------------------------------------------");
                System.out.println("( abrir + nome do arquivo) Abrir um Arquivo");
                System.out.println("( atualizar ) Para atualizar a lista de arquivos");
                System.out.println("( sair ) Deslogar do sistema");
                System.out.println("-----------------------------------------------");
                break;
            case 4:
                menuAtual = 4;
                System.out.println("-----------------------------------------------");
                System.out.println("( 1 ) Download");
                System.out.println("( 2 ) Excluir arquivo");
                System.out.println("-----------------------------------------------");
                break;
        }
    }

    public void CarregarDadosLogIn() throws IOException, ClassNotFoundException {
        try {
            usuarios = (ArrayList<Usuario>) Sistema.CarregarSistema(dirCacheUsuario);
        } catch (FileNotFoundException ex) {
            usuarios = new ArrayList<Usuario>();
            Sistema.SalvarSistema(usuarios, dirCacheUsuario);
        }
    }

}
