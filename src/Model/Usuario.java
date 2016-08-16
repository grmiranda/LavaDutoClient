package Model;

import java.io.Serializable;

public class Usuario implements Serializable, Comparable {

    private final String name;
    private final String password;

    public Usuario(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Usuario) {
            Usuario u = (Usuario) obj;
            return (u.getName().equals(this.name)
                    && u.getPassword().equals(this.password));
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(Object o) {
        Usuario aux = (Usuario) o;
        if (aux.getName().equals(this.name)
                && aux.getPassword().equals(this.password)) {
            return 0;
        } else {
            return 1;
        }
    }
}
