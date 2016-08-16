package View;

import Controller.Controller;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String ip;
        int porta;
        Scanner input = new Scanner(System.in);
        System.out.println("---------------------------------------------------");
        System.out.println("            Digite o IP do servidor");
        System.out.println("---------------------------------------------------");
        ip = input.nextLine();
        System.out.println("---------------------------------------------------");
        System.out.println("            Digite a porta do servidor");
        System.out.println("---------------------------------------------------");
        porta = input.nextInt();
        Controller controller = new Controller(ip, porta);
        controller.iniciar();
        while(input.hasNextLine()){
            controller.RecebeMsgUsuario(input.nextLine());
        }
    }
}
