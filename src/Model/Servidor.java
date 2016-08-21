package Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Servidor implements Runnable {

    private final int port;
    private final ArrayList<String> arqOcupados;

    public Servidor(int port) {
        this.port = port;
        arqOcupados = new ArrayList<>();
    }

    @Override
    public void run() { 
        try {
            ServerSocket server = new ServerSocket(port);
            while (true) {
                Socket cliente = server.accept();
                ClienteServidor cs = new ClienteServidor(cliente, this);
                new Thread(cs).start();
            }
        } catch (IOException ex) {
        }
    }
    // metodo para a remoção de um arquivo
    synchronized private void removerArq(String nome, ClienteServidor cliente) throws IOException {
        if (arqOcupados.contains(nome)) { // caso o arquivo ja esteja em uso
            cliente.enviarMensagem("#13");
            return;
        }
        File f = new File("Compartilhados/" + nome);

        if (!f.exists()) { // caso o arquivo não exista
            cliente.enviarMensagem("#12"); // 
            return;
        }

        f.delete(); // deleta o arquivo
        cliente.enviarMensagem("#16"); // envia a confirmação da remção do arquivo
        try {
            Thread.currentThread().sleep(100); // da uma pausa na thread
        } catch (InterruptedException ex) {
        }
        Cliente.enviarLista(); // reenvia a lista de arquivos do cliente
    }

    private void enviarArq(String nome, ClienteServidor cliente) throws IOException {

        File f = new File("Compartilhados/" + nome);

        if (!f.exists()) { // verifica se o arquivo existe
            cliente.enviarMensagem("#12"); // envia a mensagem de erro caso não exista 
            return;
        }
        arqOcupados.add(nome); // adiciona o arquivo na lista de arquivos em uso
        int buffer = 5120; // tamanho do buffer para envio
        byte[] conteudo = new byte[buffer];
        FileInputStream fis = new FileInputStream(f);
        OutputStream out = cliente.getSocket().getOutputStream();
        PrintStream ps = new PrintStream(out);
        ps.println("#17:" + f.length()); // envia o codigo pra envio do arquivo com o tamanho que será enviado
        int lidos = -1;

        try {
            Thread.currentThread().sleep(300); // da uma pausa na thread
        } catch (InterruptedException ex) {
        }

        while ((lidos = fis.read(conteudo, 0, buffer)) > 0) { // cliclo de envio dos dados
            out.write(conteudo, 0, lidos);
        }
        arqOcupados.remove(nome); // remove o arquivo da lista de arquivos em uso
        out.flush();
        fis.close();
    }

    public void recebeMsgCliente(String msg, ClienteServidor cliente) throws IOException {
        String mensagem[] = msg.split(":", 2);
        switch (mensagem[0]) {
            case "#10":
                enviarArq(mensagem[1], cliente);
                break;
            case "#11":
                removerArq(mensagem[1], cliente);
                break;
        }
    }
}
