package Model;

import java.util.ArrayList;

public class Arquivo {

    private final String name;
    private ArrayList<String> ipOrigem = new ArrayList<>();

    public Arquivo(String name, String ipOrigem) {
        this.name = name;
        this.ipOrigem.add(ipOrigem);
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getIpOrigem() {
        return ipOrigem;
    }

    public void adicionarIpOrigem(String ip) {
        //verificando se já existe um ip relacionado a este arquivo
        for (String s : ipOrigem) {
            if (s.equals(ip)) {
                return;
            }
        }
        //caso o ip não esteja na lista, add ele
        this.ipOrigem.add(ip);
    }

    public void removerIpOrigem(String ipOrigem) {
        if (!ipOrigem.isEmpty()) {
            this.ipOrigem.remove(ipOrigem);
        }
    }
}
