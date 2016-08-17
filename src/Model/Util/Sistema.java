package Model.Util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Sistema {

    /**
     * Metodo que salva os dados de um objeto em um arquivo de texto
     *
     * @param obj objeto a ser salvo no HD
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void SalvarSistema(Object obj, String diretorio) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(diretorio);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(obj);
        oos.close();
        fos.close();
    }

    /**
     * Metodos que carrega os dados de um objeto que foram salvos em um arquivo
     * de texto
     *
     * @return o objeto carregado pelo arquivo
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object CarregarSistema(String diretorio) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(diretorio);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object ob = ois.readObject();
        ois.close();
        fis.close();
        return ob;
    }
}
