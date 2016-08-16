package Model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor implements Runnable {
    
    private final int port;
    
    public Servidor(int port){
        this.port = port;
    }
    
    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(port);
            while(true){
                Socket cliente = server.accept();
            }
        } catch (IOException ex) {
        }
    }
}
