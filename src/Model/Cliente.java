package Model;

import Model.Util.Sistema;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private final String dirCacheArquivo = "cacheArq.ld";         // String contendo o diretório do arquivo de cache de arquivos
    private int menuAtual;                                  // Variável de controle sobre estado do sistema
    private Scanner entradaServidor;
    private static PrintStream saidaServidor;
    private int auxLogInECadastro = 0;
    private String email, senha;
    private String arquivoSelecionado;
    private ArrayList<String> origens;

    public Cliente(String ipServidor, int portaServidor) {
        this.ipServidor = ipServidor;
        this.portaServidor = portaServidor;

        //adicionando a pasta compartilhada
        File f = new File("Compartilhados");
        if (!f.exists()) {
            f.mkdir();
        }
        f = new File("Downloads");
        if (!f.exists())
            f.mkdir();
    }

    public static void enviarLista() {
        File f = new File("Compartilhados");
        String[] arqs = f.list();
        String lista = "#15:" + arqs.length;

        for (String s : arqs) {
            lista = lista + ":" + s;
        }

        saidaServidor.println(lista);
    }

    @Override
    public void run() {
        try {
            socketServidor = new Socket(ipServidor, portaServidor);
            entradaServidor = new Scanner(socketServidor.getInputStream());
            saidaServidor = new PrintStream(socketServidor.getOutputStream());
            CarregarDadosLogIn();
            CarregarDadosArquivos();
            enviarLista();
            Menu(0);
            while (entradaServidor.hasNextLine()) {
                RecebeMsgServidor(entradaServidor.nextLine());
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("Número de porta Inválido");
        } catch (SocketException ex) {
            System.out.println("Servidor Offline");
        } catch (IOException ex) {
            System.out.println("Erro Grave");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
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
                        break;
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
                    Usuario aux = new Usuario(email, senha);
                    if (usuarios.contains(aux)) {
                        saidaServidor.println("#07:" + email);
                        Menu(3);
                    } else {
                        saidaServidor.println("#04:" + email + ":" + senha);
                    }
                }
                break;
            // Tratamento para o Cadastro
            case 2:
                if (auxLogInECadastro == 0) {
                    email = msg;
                    auxLogInECadastro++;
                    Menu(2);
                } else if (auxLogInECadastro == 1) {
                    senha = msg;
                    auxLogInECadastro--;
                    saidaServidor.println("#01:" + email + ":" + senha);
                }
                break;
            // Tratamento para o Menu de Navegação    
            case 3:
                String aux[] = msg.split(" ", 2);
                switch (aux[0]) {
                    case "abrir":
                        arquivoSelecionado = aux[1];
                        Menu(4);
                        break;
                    case "atualizar":
                        saidaServidor.println("#08");
                        break;
                    case "enviar":
                        enviarLista();
                        System.out.println("Lista de arquivos enviada");
                        Menu(3);
                        break;
                    case "sair":
                        saidaServidor.println("#09");
                        Menu(0);
                        break;
                }
                break;
            // Tratamento para o Menu do Arquivo
            case 4:
                switch (msg) {
                    case "1":
                        origens = buscarOrigem(arquivoSelecionado);
                        if (origens == null){
                            System.out.println("Arquivo nao encontrado");
                            Menu(3);
                        } else if (origens.size() == 1){
                            socketCliente = new Socket(origens.get(0), portaServidor + 1);
                            PrintStream ps = new PrintStream(socketCliente.getOutputStream());
                            ps.println("#10:"+arquivoSelecionado);
                            Scanner sc = new Scanner(socketCliente.getInputStream());
                            String m = sc.nextLine();
                            String[] info = m.split(":");
                            if (info[0].equals("#12")){
                                System.out.println("Arquivo nao encontrado");
                                System.out.println("Atualize sua lista");
                                Menu(3);
                            } else if (info[0].equals("#17")){
                                long tam = Long.parseLong(info[1]);
                                long size = 0;
                                int lidos = -1;
                                int buffer = 5120;
                                byte conteudo[] = new byte[buffer];
                                FileOutputStream fos = new FileOutputStream("Downloads/" + arquivoSelecionado);
                                InputStream is = socketCliente.getInputStream();
                                
                                while ((lidos = is.read(conteudo, 0, buffer)) > 0){
                                    fos.write(conteudo, 0, lidos);
                                    size = size + lidos;
                                    if (size == tam)
                                        break;
                                }
                                
                                fos.flush();
                                fos.close();
                                socketCliente.close();
                                System.out.println("Arquivo baixado com sucesso");
                            }
                        }
                        break;
                    case "2":
                        origens = buscarOrigem(arquivoSelecionado);
                        if (origens == null) {
                            System.out.println("Arquivo não encontrado");
                            Menu(3);
                        } else if (origens.size() == 1) {
                            socketCliente = new Socket(origens.get(0), portaServidor + 1);
                            PrintStream ps = new PrintStream(socketCliente.getOutputStream());
                            ps.println("#11:" + arquivoSelecionado);
                            Scanner sc = new Scanner(socketCliente.getInputStream());
                            String m = sc.nextLine();
                            switch (m) {
                                case "#13":
                                    System.out.println("Você nao pode remover o arquivo no momento");
                                    break;
                                case "#12":
                                    System.out.println("Arquivo não encontrado");
                                    System.out.println("Atualize a sua lista");
                                    break;
                                case "#16":
                                    System.out.println("Arquivo deletado");
                                    System.out.println("Atualize a sua lista");
                                    break;
                            }
                            socketCliente.close();
                            Menu(3);
                        } else {
                            Menu(5);
                        }
                        break;
                    case "3":
                        arquivoSelecionado = "";
                        Menu(3);
                        break;
                }
                break;
            case 5:
                socketCliente = new Socket(origens.get(Integer.parseInt(msg)-1), portaServidor + 1);
                PrintStream ps = new PrintStream(socketCliente.getOutputStream());
                ps.println("#11:" + arquivoSelecionado);
                Scanner sc = new Scanner(socketCliente.getInputStream());
                String m = sc.nextLine();
                switch (m) {
                    case "#13":
                        System.out.println("Você nao pode remover o arquivo no momento");
                        break;
                    case "#12":
                        System.out.println("Arquivo não encontrado");
                        System.out.println("Atualize a sua lista");
                        break;
                    case "#16":
                        System.out.println("Arquivo deletado");
                        System.out.println("Atualize a sua lista");
                        break;
                }
                socketCliente.close();
                Menu(3);
                break;
        }
    }

    private ArrayList<String> buscarOrigem(String nome) {
        for (Arquivo arq : arquivos) {
            if (arq.getName().equals(nome)) {
                return arq.getIpOrigem();
            }
        }
        return null;
    }

    private void RecebeMsgServidor(String msg) throws IOException {
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
                    Usuario aux = new Usuario(email, senha);
                    if (!usuarios.contains(aux)) {
                        usuarios.add(aux);
                    }
                    SalvarDadosLogIn();
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
                    Usuario aux = new Usuario(email, senha);
                    usuarios.add(aux);
                    SalvarDadosLogIn();
                } else if (mensagem[0].equals("#03")) {
                    System.out.println("Email já cadastrado");
                }
                Menu(0);
                break;
            // Tratamento para o Menu de Navegação 
            case 3:
                if (mensagem[0].equals("#14")) {
                    int quantidade = Integer.parseInt(mensagem[1]);
                    int i, posNome = 2, posIp = 3;
                    String nome, ip;
                    arquivos.clear();
                    for (i = 0; i < quantidade; i++) {
                        nome = mensagem[posNome];
                        ip = mensagem[posIp];
                        Arquivo aux = new Arquivo(nome, ip);
                        arquivos.add(aux);
                        posNome += 2;
                        posIp += 2;
                    }
                    SalvarDadosArquivos();
                    Menu(3);
                } else if (mensagem[0].equals("#06")) {
                    System.out.println("Usuario já está logado");
                    Menu(0);
                }
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
                if (!arquivos.isEmpty()) {
                    for (Arquivo arq : arquivos) {
                        System.out.println(arq.getName());
                    }
                } else {
                    System.out.println("Não existem arquivos");
                }
                System.out.println("-----------------------------------------------");
                System.out.println("( abrir + nome do arquivo) Abrir um Arquivo");
                System.out.println("( atualizar ) Para atualizar a lista de arquivos");
                System.out.println("( enviar) Para enviar a sua lista de arquivos");
                System.out.println("( sair ) Deslogar do sistema");
                System.out.println("-----------------------------------------------");
                break;
            case 4:
                menuAtual = 4;
                System.out.println("-----------------------------------------------");
                System.out.println("( 1 ) Download");
                System.out.println("( 2 ) Excluir arquivo");
                System.out.println("( 3 ) Voltar");
                System.out.println("-----------------------------------------------");
                break;
            case 5:
                int i = 1;
                System.out.println("-----------------------------------------------");
                System.out.println("Selecione o Ip de onde deseja excluir");
                for(String ip : origens){
                    System.out.println("( " + i + " )" + ip);
                    i++;
                }
                System.out.println("-----------------------------------------------");
                break;
            case 6:
                int j = 1;
                System.out.println("-----------------------------------------------");
                System.out.println("Selecione o Ip de onde deseja baixar");
                for(String ip : origens){
                    System.out.println("( " + j + " )" + ip);
                    j++;
                }
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

    public void CarregarDadosArquivos() throws IOException, ClassNotFoundException {
        try {
            arquivos = (ArrayList<Arquivo>) Sistema.CarregarSistema(dirCacheArquivo);
        } catch (FileNotFoundException ex) {
            arquivos = new ArrayList<Arquivo>();
            Sistema.SalvarSistema(arquivos, dirCacheArquivo);
        }
    }

    public void SalvarDadosLogIn() throws IOException {
        Sistema.SalvarSistema(usuarios, dirCacheUsuario);
    }

    public void SalvarDadosArquivos() throws IOException {
        Sistema.SalvarSistema(arquivos, dirCacheArquivo);
    }
}
