package Model;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Servidor implements Runnable {
    
    private final int port;
    private final ArrayList<String> arqOcupados;
    
    public Servidor(int port){
        this.port = port;
        arqOcupados = new ArrayList<>();
    }
    
    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(port);
            while(true){
                Socket cliente = server.accept();
                ClienteServidor cs = new ClienteServidor(cliente, this);
                new Thread(cs).start();
            }
        } catch (IOException ex) {
        }
    }
    
    private void removerArq(String nome, ClienteServidor cliente) throws IOException{
        if (arqOcupados.contains(nome)){
            cliente.enviarMensagem("#13");
            return;
        }
        File f = new File("Compartilhados/" + nome);
        
        if (!f.exists()){
            cliente.enviarMensagem("#12");
            return;
        }
        
        f.delete();
        cliente.enviarMensagem("#16");
        Cliente.enviarLista();
    }
    
    public void recebeMsgCliente(String msg, ClienteServidor cliente) throws IOException{
        String mensagem[] = msg.split(":", 2);
        switch(mensagem[0]){
            case "#10":
                break;
            case "#11":
                removerArq(mensagem[1], cliente);
                break;
        }
    }
}
