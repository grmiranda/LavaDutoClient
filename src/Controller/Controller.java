package Controller;

import Model.Cliente;
import Model.Servidor;
import java.io.IOException;

public class Controller {

    private String ip;
    private int porta;
    private Cliente cliente;
    private Servidor server;
    
    public Controller(String ip, int porta) {
        this.ip = ip;
        this.porta = porta;
    }

    public void iniciar() {
        cliente = new Cliente(ip, porta);
        server = new Servidor(porta+1);
        new Thread(cliente).start();
        new Thread(server).start();
    }

    public void RecebeMsgUsuario(String msg) throws IOException, ClassNotFoundException {
        cliente.RecebeMsgUsuario(msg);
    }

}
