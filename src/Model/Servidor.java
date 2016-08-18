package Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private void removerArq(String nome, ClienteServidor cliente) throws IOException {
        if (arqOcupados.contains(nome)) {
            cliente.enviarMensagem("#13");
            return;
        }
        File f = new File("Compartilhados/" + nome);

        if (!f.exists()) {
            cliente.enviarMensagem("#12");
            return;
        }

        f.delete();
        cliente.enviarMensagem("#16");
        Cliente.enviarLista();
    }

    private void enviarArq(String nome, ClienteServidor cliente) throws IOException {
        if (arqOcupados.contains(nome)) {
            cliente.enviarMensagem("#13");
            return;
        }
        File f = new File("Compartilhados/" + nome);

        if (!f.exists()) {
            cliente.enviarMensagem("#12");
            return;
        }

        int buffer = 5120;
        byte[] conteudo = new byte[buffer];
        FileInputStream fis = new FileInputStream(f);
        OutputStream out = cliente.getSocket().getOutputStream();
        PrintStream ps = new PrintStream(out);
        ps.println("#17:" + f.length());
        int lidos = -1;
        while ((lidos = fis.read(conteudo, 0, buffer)) > 0) {
            out.write(conteudo, 0, lidos);
        }
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
