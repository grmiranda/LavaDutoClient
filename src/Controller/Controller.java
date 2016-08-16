package Controller;

import Model.Cliente;
import java.io.IOException;

public class Controller {

    String ip;
    int porta;
    Cliente cliente;

    public Controller(String ip, int porta) {
        this.ip = ip;
        this.porta = porta;
    }

    public void iniciar() {
        cliente = new Cliente(ip, porta);
        cliente.run();
    }

    public void RecebeMsgUsuario(String msg) throws IOException, ClassNotFoundException {
        cliente.RecebeMsgUsuario(msg);
    }

}
