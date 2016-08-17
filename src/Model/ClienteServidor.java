package Model;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClienteServidor implements Runnable {

    private Socket cliente;
    private Servidor servidor;
    
    ClienteServidor(Socket cliente, Servidor servidor) {
        this.cliente = cliente;
        this.servidor = servidor;
    }

    @Override
    public void run() {
        try {
            Scanner input = new Scanner(cliente.getInputStream());
            while(input.hasNextLine()){
                servidor.recebeMsgCliente(input.nextLine(), this);
            }
        } catch (IOException ex) {
            
        }
    }

    public void enviarMensagem(String msg) throws IOException{
        PrintStream output = new PrintStream(cliente.getOutputStream());
        output.println(msg);
    }
}
